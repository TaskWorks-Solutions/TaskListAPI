

# TaskList API with AppDynamics Full-Stack Monitoring

This project consists of a Spring Boot Task Management API connected to a PostgreSQL database, monitored by both the AppDynamics Java APM Agent (for code-level visibility) and the AppDynamics Kubernetes Cluster Agent (for infrastructure visibility).

## Project Architecture

The monitoring ecosystem is divided into two primary areas:

1. **Java APM Agent (The Code Watcher):** Injected into the JVM of the `tasklist-backend`. It tracks response times, errors, and database calls between the API and PostgreSQL.
2. **Cluster Agent (The Infrastructure Watcher):** A Kubernetes operator-based agent that monitors the health of the MicroK8s cluster, pod restarts, and resource limits.

## Prerequisites

* MicroK8s or Docker Desktop (with Kubernetes enabled)
* Java 17 (for local development)
* AppDynamics SaaS Account (Controller URL and Access Key)

## 1. Java APM Agent Configuration (Docker Compose)

### Configure Environment

Create a `.env` file in the root directory:

```bash
DB_USERNAME=tasklist_user
DB_PASSWORD=your_secure_password

```

### Update Agent Settings

Edit `appdynamics/java-agent/conf/controller-info.xml` with your controller details:

```xml
<controller-info>
    <controller-host>your-controller.saas.appdynamics.com</controller-host>
    <controller-port>443</controller-port>
    <controller-ssl-enabled>true</controller-ssl-enabled>
    <account-name>your-account-name</account-name>
    <account-access-key>your-access-key</account-access-key>
    <application-name>TaskList-K8s-App</application-name>
    <tier-name>Backend</tier-name>
</controller-info>

```

---

## 2. Kubernetes Cluster Agent Configuration

The Cluster Agent uses a Custom Resource Definition (CRD) approach managed by the AppDynamics Operator.

### Critical Manual Patches

To ensure stability in local environments, the following patches were applied:

1. **Metrics Server:** Added `--kubelet-insecure-tls` to allow MicroK8s to report resource metrics despite self-signed certificates.
2. **Credential Injection:** Injected `APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY` directly into the deployment environment to bypass strict CRD decoding errors.

### Deploying the Cluster Agent

The configuration is stored in `20-cluster-agent.yaml`. To apply:

```bash
kubectl apply -f ~/tasklist-project/appdynamics/cluster-agent/20-cluster-agent.yaml

```

---

## 3. Post-Restart Workflow (Maintenance Routine)

If you shut down your host machine or restart WSL, follow these steps to restore monitoring:

### Start the Cluster

```bash
microk8s start
microk8s status --wait-ready

```

### Run Automated Verification

This script verifies the metrics-server, re-applies patches, and checks pod health.

```bash
~/tasklist-project/verify-appd.sh

```

### Monitor Agent Registration

Check the handshake between the cluster and the AppDynamics SaaS Controller:

```bash
kubectl logs -f -n appdynamics -l clusterAgent_cr=tasklist-cluster-agent

```

---

## 4. Daily Operations Cheat Sheet

### Monitoring and Logs

* **Follow Cluster Agent Logs:** `kubectl logs -f -n appdynamics -l clusterAgent_cr=tasklist-cluster-agent`
* **Check All Pod Status:** `kubectl get pods -A`
* **Check Java Agent Logs:** `docker exec -it tasklist-backend tail -f /opt/appdynamics/java-agent/logs/agent.log`

### Management and Troubleshooting

* **Restart Cluster Agent:** `kubectl rollout restart deployment tasklist-cluster-agent -n appdynamics`
* **Re-inject Access Key:** `kubectl set env deployment/tasklist-cluster-agent -n appdynamics APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY=x5nmpxeaod5g`
* **Verify Metrics Server:** `kubectl get deployment metrics-server -n kube-system`

---

## 5. Monitoring in AppDynamics

Once the agents are registered, navigate to your controller:

### Infrastructure (Servers > Clusters > TaskListAPI)

* **Pod Health:** View restarts, crashes, and resource pressure.
* **Inventory:** Monitor Deployments, Services, and Nodes.

### Applications (Applications > TaskList-K8s-App)

* **Flow Map:** Visual path from the API to PostgreSQL.
* **Business Transactions:** Performance of `/api/tasks` endpoints.
* **JVM Metrics:** Memory heap usage and garbage collection.

---

## Troubleshooting Checklist

1. **Error: "accessKey not specified":** The Deployment lost its environment variable. Re-run the "Re-inject Access Key" command from the cheat sheet.
2. **Dashboard shows 0 Nodes/Pods:** Ensure the `metrics-server` is running and the `--kubelet-insecure-tls` flag is present.
3. **No Java Data:** Verify `controller-info.xml` contains the correct account credentials.

### File Locations

* **Configs:** `~/tasklist-project/appdynamics/config/`
* **Verification Script:** `~/tasklist-project/verify-appd.sh`

To get the most out of your AppDynamics setup, you can run specific tests to see how the agents react. These scenarios will generate the visual data you need to verify that both the **Java APM Agent** and the **Cluster Agent** are working correctly.

---

### Scenario 1: Traffic and Flow Map Visualization

This test populates the "Flow Map" and "Business Transactions" in the Application view.

1. **Action:** Run a loop to generate multiple tasks in your API.
```bash
for i in {1..10}; do
  curl -X POST "http://localhost:8080/api/tasks" \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Task $i\",\"description\":\"Automatic Load Test\",\"status\":\"IN_PROGRESS\",\"priority\":\"MEDIUM\"}"
done

```


2. **Where to look:** **Applications > TaskList-K8s-App > Dashboard**.
3. **Expectation:** You will see a visual line (flow) connecting your **Backend** tier to your **Postgres** database. The "Calls per Minute" should increase.

---

### Scenario 2: Service Disruption (Pod Health)

This test shows how the Cluster Agent tracks infrastructure stability.

1. **Action:** Manually delete a running pod to force Kubernetes to restart it.
```bash
kubectl delete pod -n tasklist -l app=tasklist-backend

```


2. **Where to look:** **Servers > Clusters > TaskListAPI > Events**.
3. **Expectation:** You will see a "Pod Deleted" and "Pod Created" event. In the **Pods** tab, you will see the restart count increment.

---

### Scenario 3: Database Latency and SQL Visibility

This test verifies that the Java Agent is successfully "sniffing" the database calls.

1. **Action:** Fetch all tasks multiple times.
```bash
curl -X GET "http://localhost:8080/api/tasks"

```


2. **Where to look:** **Applications > TaskList-K8s-App > Database Calls** (or the **Databases** top-level menu).
3. **Expectation:** You will see the specific SQL query (e.g., `SELECT * FROM tasks`) and exactly how many milliseconds the database took to respond.

---

### Scenario 4: Resource Limits and Capacity

This test helps you identify if your Kubernetes configuration is missing safety rails.

1. **Action:** No action required if you haven't set CPU/Memory limits in your YAML.
2. **Where to look:** **Servers > Clusters > TaskListAPI > Dashboard**. Look at the "Pod Issues" or "No Resource Limits" widget.
3. **Expectation:** AppDynamics will flag your pods as "Risk" items because they don't have defined CPU or Memory limits, which could lead to node instability.

---

### Scenario 5: Errors and Exceptions

This test shows how AppDynamics captures code-level failures.

1. **Action:** Send a malformed request to the API (e.g., empty body or wrong data type).
```bash
curl -X POST "http://localhost:8080/api/tasks" -H "Content-Type: application/json" -d '{"bad-data": "true"}'

```


2. **Where to look:** **Applications > TaskList-K8s-App > Troubleshooting > Errors**.
3. **Expectation:** You will see 400 or 500 error codes recorded, and you can click into them to see the Java Stack Trace.

==============================================================================


AppDynamics Setup Documentation (End-to-End)

This document explains how AppDynamics is integrated into the project, starting from application-level monitoring (Java Agent) and extending to Kubernetes cluster-level monitoring (Cluster Agent), all managed through ArgoCD.

1. Java Agent (Application Performance Monitoring – APM)
1.1 What the Java Agent Does

The AppDynamics Java Agent monitors the application from inside the JVM.
It provides:

Business Transactions (HTTP, REST, DB calls)

Response times and errors

JVM metrics (heap, GC, threads)

Code-level visibility without changing application code

Each running pod becomes a Node inside an Application → Tier in AppDynamics.

1.2 How the Java Agent Is Added to the Container

The Java Agent is baked into the Docker image, not downloaded at runtime.

Build Process (Multi-Stage Docker Build)

Build stage

Compiles the Spring Boot application

Produces the application JAR

Runtime stage

Copies the compiled JAR

Copies the AppDynamics Java Agent into the image

COPY --chown=app:app appdynamics/java-agent/ /opt/appdynamics/java-agent/


📌 Important
The folder below must exist before building the image:

appdynamics/java-agent/


This directory contains the Java Agent downloaded from the AppDynamics portal.

1.3 How the Java Agent Is Activated (Runtime)

The agent is attached when the container starts, using JVM options supplied by Kubernetes.

Environment Variables (from Deployment)
- name: APPD_CONTROLLER_HOST
  value: "theater202601042150029.saas.appdynamics.com"
- name: APPD_CONTROLLER_PORT
  value: "443"
- name: APPD_ACCOUNT_NAME
  value: "theater202601042150029"
- name: APPD_ACCESS_KEY
  valueFrom:
    secretKeyRef:
      name: appd-access-secret
      key: access-key

JVM Options
- name: JAVA_OPTS
  value: >
    -javaagent:/opt/appdynamics/java-agent/javaagent.jar
    -Dappdynamics.agent.applicationName=TaskListAPI
    -Dappdynamics.agent.tierName=Backend
    -Dappdynamics.agent.reuse.nodeName=true
    -Dappdynamics.agent.reuse.nodeName.prefix=Backend_Node
    -Dappdynamics.agent.logs.dir=/opt/appdynamics/java-agent/logs


Result in AppDynamics:

Application: TaskListAPI

Tier: Backend

Nodes: one per running pod

1.4 Java Agent Current Status

Agent starts successfully inside the container

Agent logs show registration attempts

Application does not yet appear in the controller

Issue identified at the controller handshake / config channel stage

This confirms the agent is running, but the controller connection is not fully established yet.

2. Kubernetes Cluster Agent (Infrastructure & Cluster Visibility)
2.1 What the Cluster Agent Does

The Cluster Agent does NOT monitor application code.

Instead, it monitors the Kubernetes platform itself:

Nodes (CPU, memory, disk)

Pods, deployments, replicas

Kubernetes events

Namespace-level visibility

Container health

📌 Think of it like this:

Java Agent → What is the app doing?

Cluster Agent → What is Kubernetes doing?

2.2 Dedicated Namespace

The Cluster Agent runs in its own namespace:

appdynamics


Defined in:

appdynamics/cluster-agent/00-namespace.yaml


Why a separate namespace?

Isolation from application workloads

Clear ownership and security boundaries

Easier upgrades and management

2.3 How the Cluster Agent Sees the Entire Cluster

Even though it runs in its own namespace, it can see the whole cluster because of RBAC permissions.

Defined in:

appdynamics/cluster-agent/10-rbac.yaml


This grants read access to:

Pods

Nodes

Deployments

Services

Events (across all namespaces)

2.4 Cluster Monitoring Configuration

Defined in:

appdynamics/cluster-agent/20-clustermon.yaml


This enables:

Cluster health monitoring

Namespace and workload discovery

Metric collection from Kubernetes APIs

2.5 Secrets and Authentication

The AppDynamics access key is stored as a Kubernetes secret:

Cluster Agent:

appdynamics/cluster-agent/30-secret.yaml


Application pods:

appdynamics/appd-secret.yaml


Why secrets?

No credentials in Git

Secure injection into pods

Compatible with GitOps

3. How Java Agent and Cluster Agent Work Together

They are independent but complementary:

Component	Visibility
Java Agent	Code, transactions, JVM
Cluster Agent	Pods, nodes, cluster health

In the AppDynamics UI:

Java Agent data appears under Applications

Cluster Agent data appears under Infrastructure / Kubernetes

They share the same controller, but serve different purposes.

4. ArgoCD Integration (GitOps)

The Cluster Agent is deployed via ArgoCD:

Application name: appdynamics-cluster-agent

Source repo: TaskWorks-Solutions/TaskListAPI

Target namespace: appdynamics

Automated sync enabled:

Self-healing

Pruning

What this gives you:

One-click redeploys

Version-controlled monitoring

No manual kubectl drift

5. Application Deployment Summary

The tasklist-backend deployment includes:

3 replicas

Health probes (startup, readiness, liveness)

Java Agent attached at JVM level

Secure access to AppDynamics via secrets

Each pod becomes:

TaskListAPI → Backend → Backend_Node_x

6. Current State (Controller Perspective)
What Is Working

Java Agent runs inside containers

Agent attempts registration

Cluster Agent deployed and configured

GitOps pipeline in place