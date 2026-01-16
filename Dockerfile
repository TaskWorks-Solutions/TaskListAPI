# ===============================
# Build Stage
# ===============================
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY .github/maven-settings.xml .github/maven-settings.xml

RUN chmod +x mvnw
RUN ./mvnw -s .github/maven-settings.xml dependency:go-offline -B

COPY src src
RUN ./mvnw -s .github/maven-settings.xml clean package -DskipTests

# ===============================
# Runtime Stage
# ===============================
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

RUN groupadd -r app && useradd -r -g app app && \
    mkdir -p /opt/appdynamics/java-agent/logs && \
    chown -R app:app /opt/appdynamics

COPY --from=build /app/target/*.jar app.jar
COPY --chown=app:app appdynamics/java-agent/ /opt/appdynamics/java-agent/

RUN chmod -R 755 /opt/appdynamics/java-agent && \
    chown -R app:app /opt/appdynamics/java-agent

# Copy entrypoint script and make executable
COPY --chown=app:app docker-entrypoint.sh /app/
RUN chmod +x /app/docker-entrypoint.sh

USER app

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=300s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set default Java options for the application
ENV JAVA_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.base/java.net=ALL-UNNAMED \
  --add-opens java.base/java.io=ALL-UNNAMED \
  --add-opens java.base/java.util.concurrent.atomic=ALL-UNNAMED \
  -Dappdynamics.agent.applicationName=TaskListAPI \
  -Dappdynamics.agent.tierName=Backend \
  -Dappdynamics.agent.nodeName=Backend_Docker_Node \
  -Dappdynamics.agent.reuse.nodeName=true \
  -Dappdynamics.agent.reuse.nodeName.prefix=Backend_Node \
  -Dappdynamics.agent.logs.dir=/opt/appdynamics/java-agent/logs \
  -Dspring.profiles.active=prod"

# Set default AppDynamics agent location
ENV APPD_AGENT_JAR=/opt/appdynamics/java-agent/javaagent.jar

ENTRYPOINT ["/app/docker-entrypoint.sh"]