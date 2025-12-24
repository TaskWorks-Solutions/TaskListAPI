# Discription: 
CRUD Task Management System
Status Filtering
PostgreSQL 15
Swagger/OpenAPI
Docker support
CI/CD (GitHub Actions)
Deployment on microk8s + ArgoCD
Configuration Management with Ansible
Functional Testing with JMeter
Dev environment: WSL2 on Ubuntu

# Project Structure
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
 │    │     └── TaskRequest.java
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
 │    ├── db/migration/V1__init.sql   (Flyway)
 │
 ├── Dockerfile
 ├── docker-compose.yml
 ├── pom.xml
 └── README.md

# Ansible Playbooks
ansible/
 ├── playbooks/
 │   ├── install_microk8s.yaml
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

# maven compile
mvn clean package

# maven test
mvn clean test

# if you wanna skip the mvn test
mvn clean package -DskipTests

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
