output "instance_public_ip" {
  description = "Public IP of the EC2 instance"
  value       = aws_instance.vocalyx_server.public_ip
}

output "instance_public_dns" {
  description = "Public DNS of the EC2 instance"
  value       = aws_instance.vocalyx_server.public_dns
}

output "frontend_url" {
  description = "Vocalyx frontend URL"
  value       = "http://${aws_instance.vocalyx_server.public_ip}:30080"
}

output "grafana_url" {
  description = "Grafana dashboard URL"
  value       = "http://${aws_instance.vocalyx_server.public_ip}:30030"
}

output "ssh_command" {
  description = "SSH command to connect to the instance"
  value       = "ssh -i ~/.ssh/vocalyx-key ubuntu@${aws_instance.vocalyx_server.public_ip}"
}