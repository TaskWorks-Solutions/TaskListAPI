# TaskListAPI Kubernetes Deployment

This directory contains the Kubernetes manifests and configurations for deploying the TaskListAPI application with its dependencies.

## Directory Structure

```
k8s/
├── base/                  # Base Kubernetes manifests
│   ├── deployment.yaml    # Main application deployment
│   ├── service.yaml       # Service definition
│   ├── ingress.yaml       # Ingress configuration
│   ├── postgres.yaml      # PostgreSQL database
│   └── secret.yaml        # Kubernetes secrets
└── argocd/               # ArgoCD application definitions
    ├── appdynamics-app.yaml  # AppDynamics monitoring
    └── application.yaml      # Main application
```

## Prerequisites

1. **WSL with Ubuntu**
2. **MicroK8s** (lightweight Kubernetes)
3. **kubectl** (Kubernetes CLI)
4. **Docker** (for building images)

## Setting Up Kubernetes in WSL

### 1. Install MicroK8s

```bash
# Install MicroK8s
sudo snap install microk8s --classic

# Add your user to the microk8s group
sudo usermod -a -G microk8s $USER
sudo chown -f -R $USER ~/.kube

# Apply changes by logging out and back in, or run:
newgrp microk8s

# Check status (wait for all services to be ready)
microk8s status --wait-ready

# Enable required addons
microk8s enable dns storage ingress
```

### 2. Set Up kubectl Alias

Add this to your `~/.bashrc` or `~/.zshrc`:

```bash
alias kubectl='microk8s kubectl'
alias k='microk8s kubectl'
export KUBECONFIG=~/.kube/config
```

Then reload your shell:
```bash
source ~/.bashrc  # or source ~/.zshrc
```

### 3. Access Kubernetes Dashboard (Optional)

```bash
microk8s dashboard-proxy
```

This will start the dashboard and open it in your default browser.

## Deploying the Application

### 1. Create Namespace and Secrets

```bash
# Create the tasklist namespace
kubectl create namespace tasklist

# Apply the secret (update values in base/secret.yaml first)
kubectl apply -f k8s/base/secret.yaml -n tasklist
```

### 2. Deploy PostgreSQL

```bash
# Deploy PostgreSQL
kubectl apply -f k8s/base/postgres.yaml -n tasklist

# Wait for PostgreSQL to be ready
kubectl wait --for=condition=ready pod -l app=postgres -n tasklist --timeout=120s
```

### 3. Deploy the Application

```bash
# Deploy the application
kubectl apply -f k8s/base/deployment.yaml -n tasklist
kubectl apply -f k8s/base/service.yaml -n tasklist
kubectl apply -f k8s/base/ingress.yaml -n tasklist
```

### 4. Verify Deployment

```bash
# Check all resources
kubectl get all -n tasklist

# View application logs
kubectl logs -f deployment/tasklist-backend -n tasklist

# Check database connection
kubectl exec -it deployment/tasklist-backend -n tasklist -- pg_isready -h tasklist-postgres -U postgres
```

## Accessing the Application

### Port Forwarding

```bash
# Forward service port to localhost
kubectl port-forward svc/tasklist-service 8080:80 -n tasklist
```

Then access the application at: http://localhost:8080

### Using Ingress (if configured)

If you have an ingress controller installed and configured, you can access the application via the ingress hostname.

## Managing the Cluster

### Common Commands

```bash
# Get cluster info
kubectl cluster-info

# Get all resources in all namespaces
kubectl get all --all-namespaces

# Describe a resource
kubectl describe pod <pod-name> -n tasklist

# Delete a resource
kubectl delete -f k8s/base/deployment.yaml
```

### Troubleshooting

1. **Pods not starting**
   ```bash
   # Check pod status
   kubectl describe pod <pod-name> -n tasklist
   
   # Check pod logs
   kubectl logs <pod-name> -n tasklist
   ```

2. **Reset MicroK8s (if needed)**
   ```bash
   microk8s reset
   microk8s start
   ```

## Using ArgoCD (Optional)

If you have ArgoCD installed, you can manage your application using GitOps:

```bash
# Apply ArgoCD application
kubectl apply -f k8s/argocd/appdynamics-app.yaml

# Get ArgoCD admin password
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d

# Port forward ArgoCD UI
kubectl port-forward svc/argocd-server -n argocd 8080:80
```

Access ArgoCD at: http://localhost:8080

## Clean Up

To remove all resources:

```bash
kubectl delete -k k8s/base
# Or
kubectl delete -k k8s/overlays/dev
```

## Notes

- The application runs in the `tasklist` namespace
- All sensitive data should be managed using Kubernetes Secrets
- For production, consider using sealed-secrets or external secret management