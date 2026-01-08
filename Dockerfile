# ===============================
# Build Stage
# ===============================
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

# Copy Maven wrapper and settings
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY .github/maven-settings.xml .github/maven-settings.xml

RUN chmod +x mvnw
RUN ./mvnw -s .github/maven-settings.xml dependency:go-offline -B

# Copy source code and build
COPY src src
RUN ./mvnw -s .github/maven-settings.xml clean package -DskipTests

# ===============================
# Runtime Stage
# ===============================
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Install curl for healthchecks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r app && useradd -r -g app app

# Create AppDynamics directory and set permissions
RUN mkdir -p /opt/appdynamics/java-agent/ver25.12.0.37551/logs && \
    chown -R app:app /opt/appdynamics

# Copy the built application JAR
COPY --from=build /app/target/*.jar app.jar

# Copy AppDynamics agent files
COPY --chown=app:app appdynamics/java-agent/javaagent.jar /opt/appdynamics/java-agent/javaagent.jar
COPY --chown=app:app appdynamics/java-agent/ver25.12.0.37551 /opt/appdynamics/java-agent/ver25.12.0.37551

# Set ownership
RUN chown -R app:app /app /opt/appdynamics

# Switch to non-root user
USER app

# AppDynamics environment variables
ENV APPDYNAMICS_AGENT_APPLICATION_NAME=TaskListAPI
ENV APPDYNAMICS_AGENT_TIER_NAME=Backend
ENV APPDYNAMICS_AGENT_NODE_NAME=LocalDocker
ENV APPDYNAMICS_CONTROLLER_HOST_NAME=theater202601042150029.saas.appdynamics.com
ENV APPDYNAMICS_CONTROLLER_PORT=443
ENV APPDYNAMICS_CONTROLLER_SSL_ENABLED=true
ENV APPDYNAMICS_AGENT_ACCOUNT_NAME=theater202601042150029
ENV APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY=x5nmpxeaod5g

# Expose application port
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Entrypoint
ENTRYPOINT ["sh", "-c", "java \
  -javaagent:/opt/appdynamics/java-agent/javaagent.jar \
  -Dappdynamics.controller.hostName=$APPDYNAMICS_CONTROLLER_HOST_NAME \
  -Dappdynamics.controller.port=$APPDYNAMICS_CONTROLLER_PORT \
  -Dappdynamics.controller.ssl.enabled=$APPDYNAMICS_CONTROLLER_SSL_ENABLED \
  -Dappdynamics.agent.accountName=$APPDYNAMICS_AGENT_ACCOUNT_NAME \
  -Dappdynamics.agent.accountAccessKey=$APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY \
  -Dappdynamics.agent.applicationName=$APPDYNAMICS_AGENT_APPLICATION_NAME \
  -Dappdynamics.agent.tierName=$APPDYNAMICS_AGENT_TIER_NAME \
  -Dappdynamics.agent.nodeName=$APPDYNAMICS_AGENT_NODE_NAME \
  -Dspring.profiles.active=prod \
  -jar app.jar"]