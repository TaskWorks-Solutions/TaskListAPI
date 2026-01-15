# TaskList API with AppDynamics Monitoring

A Spring Boot Task Management API with PostgreSQL database and AppDynamics Java APM Agent integration for performance monitoring.

## 📋 Prerequisites

- Docker & Docker Desktop (with Compose)
- Java 17 (for local development)
- AppDynamics SaaS Account (Controller URL and Access Key)

## 🛠️ Setup & Installation

### 1. Configure Environment

Create a `.env` file in the root directory with the following variables:

```bash
DB_USERNAME=tasklist_user
DB_PASSWORD=your_secure_password
```

### 2. AppDynamics Agent Configuration

The agent files are located in `/appdynamics/java-agent/`. Ensure the directory contains:

- `javaagent.jar` - Main agent JAR
- `java9-impl.jar` - Required for Java 17
- `lib/` - Core agent libraries
- `conf/` - Configuration files
- `ver25.12.0.37551/` - Version-specific binaries

### 3. Update Agent Configuration

Edit `appdynamics/java-agent/conf/controller-info.xml` with your AppDynamics controller details:

```xml
<controller-info>
    <controller-host>your-controller.saas.appdynamics.com</controller-host>
    <controller-port>443</controller-port>
    <controller-ssl-enabled>true</controller-ssl-enabled>
    <account-name>your-account-name</account-name>
    <account-access-key>your-access-key</account-access-key>
    <application-name>TaskListAPI</application-name>
    <tier-name>Backend</tier-name>
</controller-info>
```

### 4. Run the Application

Build and start the entire stack (Database + API + Agent):

```bash
# Stop any existing containers and clean volumes
docker-compose down -v

# Build and start
docker-compose up -d --build
```

## 🔍 Verifying the Deployment

### Check Logs

Wait about 30-60 seconds, then check the logs to ensure the Agent has registered:

```bash
docker logs -f tasklist-backend
```

Look for: `Started AppDynamics Java Agent Successfully`

### Health Check

Verify the API is live:

```bash
curl -X GET "http://localhost:8080/actuator/health" -H "accept: */*"
```

## 🧪 Demo: Populating Data (Traffic Simulation)

To see data in your AppDynamics Flow Map, run these commands to create sample tasks:

```bash
# Create sample tasks
curl -X POST "http://localhost:8080/api/tasks" \
  -H "Content-Type: application/json" \
  -d '{"title":"Setup AppD","description":"Verify Agent","status":"COMPLETED","priority":"HIGH"}'

curl -X POST "http://localhost:8080/api/tasks" \
  -H "Content-Type: application/json" \
  -d '{"title":"Database Test","description":"Check Migrations","status":"IN_PROGRESS","priority":"MEDIUM"}'

# View all tasks
curl -X GET "http://localhost:8080/api/tasks" -H "accept: */*"
```

## 📊 Monitoring in AppDynamics

After running the demo tasks, log into your AppDynamics Controller to view:

1. **Application Flow Map**: Visual path from the API to PostgreSQL
2. **Business Transactions**: Performance of API endpoints
3. **Database Visibility**: SQL queries and their performance
4. **JVM Metrics**: Memory, threads, and garbage collection

## 🔄 Common Operations

### Restart the Application

```bash
docker-compose restart backend
```

### View AppDynamics Logs

```bash
# View logs in real-time
docker exec -it tasklist-backend tail -f /opt/appdynamics/java-agent/logs/ver25.12.0.37551/LocalDocker/agent.log

# Or copy logs to host
docker cp tasklist-backend:/opt/appdynamics/java-agent/logs ./appdynamics-logs
```

### Stop the Application

```bash
docker-compose down
```

## 🚨 Troubleshooting

- **Agent Not Starting**: Check `docker logs tasklist-backend` for Java agent errors
- **No Data in Controller**: Verify network connectivity to AppDynamics controller
- **Database Issues**: Check PostgreSQL logs with `docker logs tasklist-postgres`

## 📚 Additional Resources

- [AppDynamics Documentation](https://docs.appdynamics.com/)
- [Spring Boot with AppDynamics](https://docs.appdynamics.com/21.3/en/application-monitoring/install-app-server-agents/java-agent)
- [Docker Compose Reference](https://docs.docker.com/compose/)
```

