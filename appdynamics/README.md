

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

