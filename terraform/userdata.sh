#!/bin/bash
set -e

LOG_FILE="/var/log/vocalyx-setup.log"
exec > >(tee -a $LOG_FILE) 2>&1

echo "=== Vocalyx Setup Started: $(date) ==="

# --- System update ---
apt-get update -y
apt-get install -y curl wget git apt-transport-https ca-certificates gnupg lsb-release

# --- Docker ---
echo "=== Installing Docker ==="
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
apt-get update -y
apt-get install -y docker-ce docker-ce-cli containerd.io
systemctl enable docker
systemctl start docker
usermod -aG docker ubuntu

# --- kubectl ---
echo "=== Installing kubectl ==="
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.29/deb/Release.key | gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg
echo "deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.29/deb/ /" | tee /etc/apt/sources.list.d/kubernetes.list
apt-get update -y
apt-get install -y kubectl

# --- Minikube ---
echo "=== Installing Minikube ==="
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
install -o root -g root -m 0755 minikube-linux-amd64 /usr/local/bin/minikube
rm minikube-linux-amd64

# --- Helm ---
echo "=== Installing Helm ==="
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# --- Start Minikube as ubuntu user ---
echo "=== Starting Minikube ==="
sudo -u ubuntu minikube start \
  --driver=docker \
  --cpus=2 \
  --memory=3500 \
  --kubernetes-version=v1.29.0

# --- Clone your repo ---
echo "=== Cloning Vocalyx repo ==="
sudo -u ubuntu git clone https://github.com/Riviya/Vocalyx.git /home/ubuntu/vocalyx
cd /home/ubuntu/vocalyx

# --- Apply Kubernetes manifests ---
echo "=== Deploying application ==="
sudo -u ubuntu bash -c '
  export KUBECONFIG=/home/ubuntu/.kube/config

  # Secrets and config
  kubectl apply -f k8s/base/mysql-secret.yml
  kubectl apply -f k8s/base/backend-secret.yml
  kubectl apply -f k8s/base/backend-configmap.yml

  # Storage
  kubectl apply -f k8s/base/mysql-pvc.yml

  # Workloads
  kubectl apply -f k8s/base/mysql-deployment.yml
  kubectl rollout status deployment/mysql --timeout=180s

  kubectl apply -f k8s/base/backend-deployment.yml
  kubectl rollout status deployment/backend --timeout=300s

  kubectl apply -f k8s/base/frontend-deployment.yml
  kubectl rollout status deployment/frontend --timeout=120s

  # Monitoring namespace
  kubectl create namespace monitoring

  # Helm repos
  helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
  helm repo add grafana https://grafana.github.io/helm-charts
  helm repo update

  # Install monitoring stack
  helm install prometheus-stack prometheus-community/kube-prometheus-stack \
    --namespace monitoring \
    --values /home/ubuntu/vocalyx/k8s/base/prometheus-values.yml \
    --timeout 10m

  helm install loki-stack grafana/loki-stack \
    --namespace monitoring \
    --values /home/ubuntu/vocalyx/k8s/base/loki-values.yml \
    --timeout 5m
'

echo "=== Vocalyx Setup Complete: $(date) ==="