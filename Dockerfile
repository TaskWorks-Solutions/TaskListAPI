# ===============================
# Build stage
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
# Runtime stage
# ===============================
FROM eclipse-temurin:17-jre-jammy

# Install curl for healthchecks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r app && useradd -r -g app app

WORKDIR /app

# Copy application jar
COPY --from=build /app/target/*.jar app.jar

# Copy AppDynamics agent
COPY --chown=app:app appdynamics/java-agent/ /opt/appdynamics/java-agent/

# Set ownership
RUN chown -R app:app /app /opt/appdynamics/java-agent

# Switch to non-root user
USER app

# Expose port
EXPOSE 8080

# ===============================
# AppDynamics environment variables baked into image
# ===============================
ENV JAVA_OPTS="-javaagent:/opt/appdynamics/java-agent/javaagent.jar"
ENV APPDYNAMICS_AGENT_APPLICATION_NAME=TaskListAPI
ENV APPDYNAMICS_AGENT_TIER_NAME=Backend
ENV APPDYNAMICS_AGENT_NODE_NAME=LocalDocker
ENV APPDYNAMICS_CONTROLLER_HOST_NAME=theater202601042150029.saas.appdynamics.com
ENV APPDYNAMICS_CONTROLLER_PORT=443
ENV APPDYNAMICS_CONTROLLER_SSL_ENABLED=true
ENV APPDYNAMICS_AGENT_ACCOUNT_NAME=theater202601042150029
ENV APPDYNAMICS_AGENT_ACCOUNT_ACCESS_KEY=x5nmpxeaod5g

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Entrypoint
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=prod -jar app.jar"]
