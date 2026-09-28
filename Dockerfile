FROM eclipse-temurin:21-jdk-alpine AS builder
RUN apk add --no-cache bash wget unzip
RUN wget -q https://services.gradle.org/distributions/gradle-8.10-bin.zip -O /tmp/gradle.zip && \
    unzip -q /tmp/gradle.zip -d /opt
ENV PATH="/opt/gradle-8.10/bin:${PATH}"
WORKDIR /workspace
COPY . .
# Build ALL JARs in a single pass to save memory and CPU (avoids 10 parallel Gradle daemons crashing Docker Desktop)
RUN gradle bootJar -x test --no-daemon -Dorg.gradle.jvmargs="-Xmx1536m -XX:MaxMetaspaceSize=512m"

# 1. Gateway
FROM eclipse-temurin:21-jre-alpine AS gateway
WORKDIR /app
COPY --from=builder /workspace/gateway/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# 2. Identity
FROM eclipse-temurin:21-jre-alpine AS identity-service
WORKDIR /app
COPY --from=builder /workspace/services/identity/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

# 3. Event
FROM eclipse-temurin:21-jre-alpine AS event-service
WORKDIR /app
COPY --from=builder /workspace/services/event/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]

# 4. Submission
FROM eclipse-temurin:21-jre-alpine AS submission-service
WORKDIR /app
COPY --from=builder /workspace/services/submission/build/libs/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]

# 5. Judging
FROM eclipse-temurin:21-jre-alpine AS judging-service
WORKDIR /app
COPY --from=builder /workspace/services/judging/build/libs/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]

# 6. Voting
FROM eclipse-temurin:21-jre-alpine AS voting-service
WORKDIR /app
COPY --from=builder /workspace/services/voting/build/libs/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]

# 7. Notification
FROM eclipse-temurin:21-jre-alpine AS notification-service
WORKDIR /app
COPY --from=builder /workspace/services/notification/build/libs/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]

# 8. Webhook Dispatcher
FROM eclipse-temurin:21-jre-alpine AS webhook-dispatcher
WORKDIR /app
COPY --from=builder /workspace/services/webhook-dispatcher/build/libs/*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]

# 9. Certificate
FROM eclipse-temurin:21-jre-alpine AS certificate-service
WORKDIR /app
COPY --from=builder /workspace/services/certificate/build/libs/*.jar app.jar
VOLUME /app/keys
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]

# 10. Observability
FROM eclipse-temurin:21-jre-alpine AS observability-service
WORKDIR /app
COPY --from=builder /workspace/services/observability/build/libs/*.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
