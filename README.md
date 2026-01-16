# TaskList API

A comprehensive Task Management System with monitoring and deployment capabilities.

## 🚀 Features

- CRUD Task Management
- Status Filtering
- PostgreSQL 15 Database
- Swagger/OpenAPI Documentation
- Docker & Docker Compose Support
- CI/CD with GitHub Actions
- Kubernetes Deployment (microk8s + ArgoCD)
- Configuration Management with Ansible
- Performance Monitoring with AppDynamics
- Functional Testing with JMeter
- Development Environment: WSL2 on Ubuntu

# Project Structure
```
TaskListAPI/
 ├── src/main/java/com/example/TaskListAPI
 │    ├── controller
 │    │     └── TaskController.java
 │    ├── service
 │    │     └── TaskService.java
 │    │     └── impl/TaskServiceImpl.java
 │    ├── repository
 │    │     └── TaskRepository.java
 │    ├── dto
 │    │     ├── TaskRequest.java
 │    │     └── TaskResponse.java
 │    ├── entity
 │    │     └── Task.java
 │    ├── enums
 │    │     └── Status.java (PENDING, IN_PROGRESS...)
 │    ├── mapper
 │    │     └── TaskMapper.java
 │    ├── exception
 │    │     ├── TaskNotFoundException.java
 │    │     └── GlobalExceptionHandler.java
 │    └── TaskManagerApplication.java
 │
 ├── src/main/resources
 │    ├── application.yaml
 │    └── db/migration/V1__init.sql   (Flyway)
 │
 ├── appdynamics/
 │    ├── java-agent/                # AppDynamics Java Agent
 │    │    ├── conf/
 │    │    │   └── controller-info.xml  # AppDynamics config
 │    │    ├── javaagent.jar
 │    │    └── ...
 │    └── README.md                  # AppDynamics setup guide
 │
 ├── ansible/                        # Ansible playbooks
 ├── k8s/                            # Kubernetes manifests
 ├── Dockerfile
 ├── docker-compose.yml
 ├── pom.xml
 └── README.md
```

# Ansible Playbooks
ansible/
 ├── playbooks/
 │   ├── setup_cluster.yaml
 │   ├── deploy_app.yaml
 │   └── rollback.yaml
 ├── inventory/
 │   └── hosts.ini

# manifests structure
k8s/
 ├── base/
 │    ├── deployment.yaml
 │    ├── service.yaml
 │    ├── ingress.yaml
 │    ├── configmap.yaml
 │    └── secret.yaml
 └── overlays/
      ├── dev/
      └── prod/

## 🛠️ Build & Run

### Local Development

```bash
# Build the application
mvn clean package

# Run tests
mvn clean test

# Skip tests during build
mvn clean package -DskipTests

# Run with Maven
mvn spring-boot:run
```

### Docker

```bash
# Build and start containers
docker-compose up -d --build

# Stop containers
docker-compose down

# View logs
docker-compose logs -f
```

## 📊 Monitoring with AppDynamics

The application includes AppDynamics Java Agent for performance monitoring. To set up:

1. Update `appdynamics/java-agent/conf/controller-info.xml` with your AppDynamics credentials
2. The agent is automatically configured in the Docker setup
3. Access your AppDynamics dashboard to monitor application performance

For detailed setup instructions, see [appdynamics/README.md](appdynamics/README.md)

# build the container run it once for the first tim
 docker-compose up -d --build     

# bring the container up
docker-compose up -d

# stop the container
docker-compose down

# run the project with maven
mvn spring-boot:run

# microk8s
# Apply manifests in microk8s:
microk8s kubectl apply -f k8s/base/secret.yaml
microk8s kubectl apply -f k8s/base/deployment.yaml
microk8s kubectl apply -f k8s/base/service.yaml
microk8s kubectl apply -f k8s/base/ingress.yaml
# OR 
kubectl apply -f k8s/base/secret.yaml
kubectl apply -f k8s/base/deployment.yaml
kubectl apply -f k8s/base/service.yaml
kubectl apply -f k8s/base/ingress.yaml

# Verify pods:
microk8s kubectl get pods
microk8s kubectl get svc
microk8s kubectl get ingress
# OR
kubectl get pods
kubectl get svc
kubectl get ingress


# AgroCD 
`Create namespace`
kubectl create namespace argocd

`Install Argo CD`
kubectl apply -n argocd \
  -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

`Verify`
kubectl get pods -n argocd

`Port Forward (simplest)`
kubectl port-forward svc/argocd-server -n argocd 8085:443
`Access:`
https://localhost:8085

`Get Argo CD Admin Password`
kubectl get secret argocd-initial-admin-secret \
  -n argocd \
  -o jsonpath="{.data.password}" | base64 --decode

username: admin


# Ansible

setup_cluster.yaml (Bootstrap Kubernetes + Argo CD)
✔ Creates namespaces
✔ Installs Argo CD
✔ Waits for readiness
run
`ansible-playbook -i ansible/inventory/hosts.ini ansible/playbooks/setup_cluster.yaml`

deploy_app.yaml (GitOps Trigger)
✔ Applies Argo CD Application
✔ Argo deploys your app
run
`ansible-playbook -i ansible/inventory/hosts.ini ansible/playbooks/deploy_app.yaml`

rollback.yaml
This performs a GitOps rollback using Argo CD history.
run
ansible-playbook \
  -i ansible/inventory/hosts.ini \
  ansible/playbooks/rollback.yaml \
  -e revision=HEAD~1


# If your wsl doesn't use systemd by default. Let's just add MetalLB to your existing cluster if you have one
# Step 1: Install MetalLB
`kubectl apply -f https://raw.githubusercontent.com/metallb/metallb/v0.14.3/config/manifests/metallb-native.yaml`
# Step 2: Wait for MetalLB
kubectl wait --namespace metallb-system \
  --for=condition=ready pod \
  --selector=app=metallb \
  --timeout=90s
# Step 4: Check Your Network
`kubectl get nodes -o wide`
# Step 4: Configure IP Address Pool
`mine is 192.168.49.2`
# NB paste it in your terminal but confirm you IP Address Pool

cat <<EOF | kubectl apply -f -
apiVersion: metallb.io/v1beta1
kind: IPAddressPool
metadata:
  name: default-pool
  namespace: metallb-system
spec:
  addresses:
  - 192.168.49.100-192.168.49.110  `patse your IP Address Here`
---
apiVersion: metallb.io/v1beta1
kind: L2Advertisement
metadata:
  name: default
  namespace: metallb-system
spec:
  ipAddressPools:
  - default-pool
EOF

# Step 5: Verify LoadBalancer Gets IP
`kubectl get svc -n ingress-nginx ingress-nginx-controller`
# Step 6: Check Ingress
`kubectl get ingress -n tasklist`
# Step 7: Update Hosts File
# Get the LoadBalancer IP
LB_IP=$(kubectl get svc -n ingress-nginx ingress-nginx-controller -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
# Add to hosts
`echo "$LB_IP tasklist.local" | sudo tee -a /etc/hosts`
