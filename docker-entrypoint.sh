#!/bin/sh
set -e

# Defaults (override with env)
SPRING_PROFILE="${SPRING_PROFILES_ACTIVE:-prod}"
APPD_AGENT_JAR="${APPD_AGENT_JAR:-/opt/appdynamics/javaagent.jar}"
APPD_CONTROLLER_HOST="${APPD_CONTROLLER_HOST:-theater202601042150029.saas.appdynamics.com}"
APPD_CONTROLLER_PORT="${APPD_CONTROLLER_PORT:-443}"
APPD_ACCOUNT_NAME="${APPD_ACCOUNT_NAME:-theater202601042150029}"
# POD_NAME / NODE_NAME populated by Downward API in k8s deployment
# APPD_ACCESS_KEY should come from secret (see k8s/base/secret.yaml)
JAVA_OPTS="${JAVA_OPTS:-}"

# Build AppDynamics JVM args only if controller host is provided
if [ -n "$APPD_CONTROLLER_HOST" ]; then
  JAVA_OPTS="$JAVA_OPTS -javaagent:${APPD_AGENT_JAR}"
  JAVA_OPTS="$JAVA_OPTS -Dappdynamics.controller.hostName=${APPD_CONTROLLER_HOST}"
  JAVA_OPTS="$JAVA_OPTS -Dappdynamics.controller.port=${APPD_CONTROLLER_PORT}"
  JAVA_OPTS="$JAVA_OPTS -Dappdynamics.agent.accountName=${APPD_ACCOUNT_NAME}"
  JAVA_OPTS="$JAVA_OPTS -Dappdynamics.agent.accountAccessKey=${APPD_ACCESS_KEY}"
  # Use POD_NAME if set; fall back to NODE_NAME
  NODE_ID="${POD_NAME:-${NODE_NAME:-unknown}}"
  JAVA_OPTS="$JAVA_OPTS -Dappdynamics.agent.nodeName=${NODE_ID}"
fi

exec java $JAVA_OPTS -Dspring.profiles.active="${SPRING_PROFILE}" -jar /app/app.jar
