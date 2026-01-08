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

# Create non-root user and set up AppDynamics directory structure
RUN groupadd -r app && useradd -r -g app app && \
    mkdir -p /opt/appdynamics/java-agent/logs && \
    chown -R app:app /opt/appdynamics

# Copy the built application JAR
COPY --from=build /app/target/*.jar app.jar

# Copy AppDynamics agent files
COPY --chown=app:app appdynamics/java-agent/javaagent.jar /opt/appdynamics/java-agent/javaagent.jar
COPY --chown=app:app appdynamics/java-agent/ver25.12.0.37551 /opt/appdynamics/java-agent/ver25.12.0.37551

# Set proper permissions for AppDynamics
RUN chmod -R 755 /opt/appdynamics/java-agent && \
    chmod 644 /opt/appdynamics/java-agent/javaagent.jar && \
    chmod -R 755 /opt/appdynamics/java-agent/ver25.12.0.37551

# Set ownership
RUN chown -R app:app /app /opt/appdynamics

# Switch to non-root user
USER app

# AppDynamics environment variables
ENV APPDYNAMICS_AGENT_APPLICATION_NAME=TaskListAPI
ENV APPDYNAMICS_AGENT_TIER_NAME=Backend
ENV APPDYNAMICS_AGENT_NODE_NAME=LocalDocker-${HOSTNAME}
ENV APPDYNAMICS_CONTROLLER_HOST_NAME=theater202601042150029.saas.appdynamics.com
ENV APPDYNAMICS_CONTROLLER_PORT=443
ENV APPDYNAMICS_CONTROLLER_SSL_ENABLED=true
ENV APPDYNAMICS_AGENT_ACCOUNT_NAME=theater202601042150029
ENV APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY=x5nmpxeaod5g
ENV APPDYNAMICS_AGENT_LOGGER_ADAPTER_VERBOSE_MODE=true
ENV APPDYNAMICS_AGENT_LOGGER_IMPL=com.singularity.ee.agent.appagent.log4j2.Log4J2LoggerImpl
ENV APPDYNAMICS_AGENT_MAX_METRICS=2000
ENV APPDYNAMICS_AGENT_REUSE_NODE_NAME=true
ENV APPDYNAMICS_AGENT_REUSE_NODE_NAME_PREFIX=LocalDocker
ENV APPDYNAMICS_AGENT_LOG_DIR=/opt/appdynamics/java-agent/logs
ENV APPDYNAMICS_AGENT_BASE_DIR=/opt/appdynamics/java-agent
ENV APPDYNAMICS_JAVA_AGENT_REUSE_NODE_NAME=true
ENV APPDYNAMICS_AGENT_UNIQUE_HOST_ID=${HOSTNAME}
ENV APPDYNAMICS_AGENT_ENABLE_LOGGING_ACTIVITY=true

# Expose application port
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Entrypoint with Java 17 compatibility flags
ENTRYPOINT ["sh", "-c", "java \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.base/java.net=ALL-UNNAMED \
  --add-opens java.base/java.io=ALL-UNNAMED \
  --add-opens java.base/java.util.concurrent.atomic=ALL-UNNAMED \
  -javaagent:/opt/appdynamics/java-agent/javaagent.jar \
  -Dappdynamics.agent.logs.dir=$APPDYNAMICS_AGENT_LOG_DIR \
  -Dappdynamics.agent.uniqueHostId=$APPDYNAMICS_AGENT_UNIQUE_HOST_ID \
  -Dappdynamics.agent.reuse.nodeName=true \
  -Dappdynamics.agent.reuse.nodeName.prefix=$APPDYNAMICS_AGENT_REUSE_NODE_NAME_PREFIX \
  -Dappdynamics.controller.hostName=$APPDYNAMICS_CONTROLLER_HOST_NAME \
  -Dappdynamics.controller.port=$APPDYNAMICS_CONTROLLER_PORT \
  -Dappdynamics.controller.ssl.enabled=$APPDYNAMICS_CONTROLLER_SSL_ENABLED \
  -Dappdynamics.agent.accountName=$APPDYNAMICS_AGENT_ACCOUNT_NAME \
  -Dappdynamics.agent.accountAccessKey=$APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY \
  -Dappdynamics.agent.applicationName=$APPDYNAMICS_AGENT_APPLICATION_NAME \
  -Dappdynamics.agent.tierName=$APPDYNAMICS_AGENT_TIER_NAME \
  -Dappdynamics.agent.nodeName=$APPDYNAMICS_AGENT_NODE_NAME \
  -Dappdynamics.agent.maxMetrics=$APPDYNAMICS_AGENT_MAX_METRICS \
  -Dappdynamics.agent.logging.verbose=$APPDYNAMICS_AGENT_LOGGER_ADAPTER_VERBOSE_MODE \
  -Dappdynamics.agent.logging.log4j2.logger.impl=$APPDYNAMICS_AGENT_LOGGER_IMPL \
  -Dappdynamics.agent.enableLoggingActivity=$APPDYNAMICS_AGENT_ENABLE_LOGGING_ACTIVITY \
  -Dspring.profiles.active=prod \
  -jar app.jar"]