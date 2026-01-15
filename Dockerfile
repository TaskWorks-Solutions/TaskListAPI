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

USER app

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=300s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Entrypoint uses standard ENV variables for AppD registration
# Entrypoint with Hardcoded Agent Properties for guaranteed registration
ENTRYPOINT ["sh", "-c", "java \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.base/java.net=ALL-UNNAMED \
  --add-opens java.base/java.io=ALL-UNNAMED \
  --add-opens java.base/java.util.concurrent.atomic=ALL-UNNAMED \
  -javaagent:/opt/appdynamics/java-agent/javaagent.jar \
  -Dappdynamics.agent.applicationName=TaskListAPI \
  -Dappdynamics.agent.tierName=Backend \
  -Dappdynamics.agent.nodeName=Backend_Docker_Node \
  -Dappdynamics.agent.reuse.nodeName=true \
  -Dappdynamics.agent.reuse.nodeName.prefix=Backend_Node \
  -Dappdynamics.agent.logs.dir=/opt/appdynamics/java-agent/logs \
  -Dspring.profiles.active=prod \
  -jar app.jar"]