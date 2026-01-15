#!/bin/bash
set -e

# This script verifies connectivity to AppDynamics Controller
# and checks if required tools are installed

echo "🔍 Validating AppDynamics connection..."

# Check if required commands are available
for cmd in curl kubectl jq; do
  if ! command -v $cmd &> /dev/null; then
    echo "❌ Error: $cmd is required but not installed"
    exit 1
  fi
done

# Get controller details from environment or use defaults
CONTROLLER_HOST=${APPD_CONTROLLER_HOST:?"APPD_CONTROLLER_HOST environment variable is not set"}
CONTROLLER_PORT=${APPD_CONTROLLER_PORT:-443}
ACCOUNT_NAME=${APPD_ACCOUNT_NAME:?"APPD_ACCOUNT_NAME environment variable is not set"}
ACCESS_KEY=${APPD_ACCESS_KEY:?"APPD_ACCESS_KEY environment variable is not set"}

# Test controller connectivity
echo "🌐 Testing connection to AppDynamics Controller at ${CONTROLLER_HOST}:${CONTROLLER_PORT}..."
if ! nc -z -w 5 ${CONTROLLER_HOST} ${CONTROLLER_PORT}; then
  echo "❌ Error: Cannot connect to ${CONTROLLER_HOST}:${CONTROLLER_PORT}"
  echo "   Please check your network settings and ensure the controller is reachable"
  exit 1
fi

# Test API access
echo "🔑 Testing API access with provided credentials..."
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" \
  -u "${ACCOUNT_NAME}@${CONTROLLER_HOST}:${ACCESS_KEY}" \
  "https://${CONTROLLER_HOST}/controller/rest/applications" \
  --insecure 2>/dev/null || true)

if [ "$RESPONSE" != "200" ]; then
  echo "❌ Error: Failed to authenticate with AppDynamics Controller"
  echo "   Status code: ${RESPONSE}"
  echo "   Please verify your account name and access key"
  exit 1
fi

# Test Kubernetes integration
echo "☸️  Testing Kubernetes integration..."
if ! kubectl get nodes &> /dev/null; then
  echo "⚠️  Warning: Could not connect to Kubernetes cluster"
  echo "   Please ensure kubectl is configured correctly"
else
  echo "✅ Successfully connected to Kubernetes cluster"
  
  # Check if Cluster Agent is running
  if kubectl get pods -n appdynamics -l app=appdynamics-cluster-agent &> /dev/null; then
    echo "✅ AppDynamics Cluster Agent is running"
  else
    echo "⚠️  AppDynamics Cluster Agent is not running"
  fi
fi

echo "✅ Validation completed successfully!"
echo "   Controller: ${CONTROLLER_HOST}"
echo "   Account: ${ACCOUNT_NAME}"
