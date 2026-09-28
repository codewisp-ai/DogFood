#!/bin/bash
# Generate multi-stage Dockerfiles for all services
# Each compiles inside Docker — zero local toolchain needed

COMMON_COPY='COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
COPY services/common services/common
COPY services/identity services/identity
COPY gateway gateway
COPY services/event services/event
COPY services/submission services/submission
COPY services/judging services/judging
COPY services/voting services/voting
COPY services/notification services/notification
COPY services/webhook-dispatcher services/webhook-dispatcher
COPY services/certificate services/certificate
COPY services/observability services/observability
COPY tests tests'

GRADLE_INSTALL='RUN chmod +x gradle/wrapper/gradle-wrapper.jar 2>/dev/null || true
RUN if [ -f gradlew ]; then ./gradlew :%MODULE%:bootJar -x test --no-daemon; \
    else apk add --no-cache bash && \
         wget -q https://services.gradle.org/distributions/gradle-8.10-bin.zip -O /tmp/gradle.zip && \
         unzip -q /tmp/gradle.zip -d /opt && \
         /opt/gradle-8.10/bin/gradle :%MODULE%:bootJar -x test --no-daemon; fi'

declare -A SERVICES
SERVICES[identity]=8081
SERVICES[event]=8082
SERVICES[submission]=8083
SERVICES[judging]=8084
SERVICES[voting]=8085
SERVICES[notification]=8086
SERVICES[webhook-dispatcher]=8087
SERVICES[certificate]=8088
SERVICES[observability]=8089

for svc in "${!SERVICES[@]}"; do
    port=${SERVICES[$svc]}
    module="services:${svc}"
    jar_path="services/${svc}/build/libs/*.jar"
    dockerfile="services/${svc}/Dockerfile"

    extra=""
    if [ "$svc" = "certificate" ]; then
        extra="VOLUME /app/keys"
    fi

    gradle_cmd=$(echo "$GRADLE_INSTALL" | sed "s|%MODULE%|${module}|g")

    cat > "$dockerfile" << DOCKERFILE
# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
${COMMON_COPY}
${gradle_cmd}

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/${jar_path} app.jar
${extra}
EXPOSE ${port}
ENTRYPOINT ["java", "-jar", "app.jar"]
DOCKERFILE

    echo "Generated $dockerfile (port $port)"
done

echo "Done — all Dockerfiles are multi-stage builds."
