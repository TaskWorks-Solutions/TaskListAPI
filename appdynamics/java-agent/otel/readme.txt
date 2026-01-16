/*
 * Copyright (C) Cisco Systems, Inc. and its affiliates
 * 2025
 * All Rights Reserved
 */

COMBINED JAVA AGENT

The Combined Java Agent provides a new feature that allows AppDynamics customers
to run the traditional AppDynamics Java Agent in combination with the
Splunk OpenTelemetry Java Agent ("Dual Signal Mode"). The Dual Signal Mode
generates both OpenTelemetry and AppDynamics signals, which can be used with
Splunk O11y Cloud and an AppDynamics controller simultaneously ("Dual Signal").

Enabling the Dual Signal Mode via the Combined Agent or otherwise will consume
more memory and CPU than running the AppDynamics Java agent in regular mode. Actual
consumption or ingestion metrics will depend on the type of application and the
application load. Please refer to the AppDynamics Documentation at
docs.appdynamics.com and Splunk Documentation at https://docs.splunk.com/observability/en/
for more information.

The Dual Signal Mode replaces the previous Hybrid Agent functionality. To enable
Dual Signal mode, simply specify
    -Dappdynamics.opentelemetry.enabled=true
or
    -Dagent.deployment.mode=dual
on the java command line.

To use the old Hybrid Mode, specify
    -Dagent.deployment.mode=hybrid
on the java command line instead.

Both components of the Combined Agent need to be configured independently.
(e.g. the target AppD controller and OTel collector host and port). For example:

-Dappdynamics.agent.applicationName=MyApplication
-Dappdynamics.agent.tierName=WebServer
-Dappdynamics.agent.nodeName=Node_22
-Dappdynamics.controller.hostName=group11.corp.appdynamics.com
-Dotel.resource.attributes=deployment.environment.name=myAccountName,service.namespace=MyApplication,service.name=WebServer
-Dotel.traces.exporter=otlp
-Dotel.metrics.exporter=otlp
-Dotel.logs.exporter=none
-Dotel.exporter.otlp.endpoint=http://localhost:4318

Additionally, you can enable OTel Mode, which activatess only the Splunk OpenTelemetry agent,
while the AppDynamics agent remains inactive:
    -Dagent.deployment.mode=otel

