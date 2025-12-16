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


 docker-compose up -d --build     