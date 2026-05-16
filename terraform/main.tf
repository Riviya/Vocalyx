terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# --- Networking ---

resource "aws_vpc" "vocalyx_vpc" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name    = "vocalyx-vpc"
    Project = "vocalyx"
  }
}

resource "aws_internet_gateway" "vocalyx_igw" {
  vpc_id = aws_vpc.vocalyx_vpc.id

  tags = {
    Name    = "vocalyx-igw"
    Project = "vocalyx"
  }
}

resource "aws_subnet" "vocalyx_public_subnet" {
  vpc_id                  = aws_vpc.vocalyx_vpc.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "${var.aws_region}a"
  map_public_ip_on_launch = true

  tags = {
    Name    = "vocalyx-public-subnet"
    Project = "vocalyx"
  }
}

resource "aws_route_table" "vocalyx_rt" {
  vpc_id = aws_vpc.vocalyx_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.vocalyx_igw.id
  }

  tags = {
    Name = "vocalyx-route-table"
  }
}

resource "aws_route_table_association" "vocalyx_rta" {
  subnet_id      = aws_subnet.vocalyx_public_subnet.id
  route_table_id = aws_route_table.vocalyx_rt.id
}

# --- Security Group ---

resource "aws_security_group" "vocalyx_sg" {
  name        = "vocalyx-sg"
  description = "Security group for Vocalyx deployment server"
  vpc_id      = aws_vpc.vocalyx_vpc.id

  # SSH access
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "SSH"
  }

  # Frontend (NodePort)
  ingress {
    from_port   = 30080
    to_port     = 30080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Vocalyx Frontend"
  }

  # Grafana (NodePort)
  ingress {
    from_port   = 30030
    to_port     = 30030
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Grafana Dashboard"
  }

  # Backend actuator (for validation)
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Spring Boot Backend"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound"
  }

  tags = {
    Name    = "vocalyx-sg"
    Project = "vocalyx"
  }
}

# --- SSH Key Pair ---

resource "aws_key_pair" "vocalyx_key" {
  key_name   = "vocalyx-key"
  public_key = file(var.public_key_path)

  tags = {
    Project = "vocalyx"
  }
}

# --- EC2 Instance ---

resource "aws_instance" "vocalyx_server" {
  ami                    = var.ami_id
  instance_type          = var.instance_type
  subnet_id              = aws_subnet.vocalyx_public_subnet.id
  vpc_security_group_ids = [aws_security_group.vocalyx_sg.id]
  key_name               = aws_key_pair.vocalyx_key.key_name

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
  }

  user_data = file("${path.module}/userdata.sh")

  tags = {
    Name    = "vocalyx-server"
    Project = "vocalyx"
  }
}