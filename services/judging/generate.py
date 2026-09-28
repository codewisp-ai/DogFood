import os

base_dir = "/Users/yash/Desktop/dogfood/services/judging"

files = {
    "build.gradle.kts": """plugins {
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    kotlin("plugin.jpa") version "1.9.23"
}

group = "com.dogfood"
version = "0.0.1-SNAPSHOT"
java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":services:common"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")
    implementation("org.postgresql:postgresql")
    implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.7.3")
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
""",
    "Dockerfile": """FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8084
ENTRYPOINT ["java","-jar","/app.jar"]
""",
    "src/main/resources/application.yml": """server:
  port: 8084
spring:
  application:
    name: judging-service
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:dogfood_judging}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
    open-in-view: false
  flyway:
    enabled: true
    schemas: judging
    create-schemas: true
    baseline-on-migrate: true
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASSWORD:guest}
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
""",
    "src/main/resources/db/migration/V1__create_judging_schema.sql": """
CREATE SCHEMA IF NOT EXISTS judging;

CREATE TABLE judging.rubrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL UNIQUE,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE judging.criteria (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rubric_id UUID REFERENCES judging.rubrics(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    weight DECIMAL(5,4) NOT NULL,
    max_score INTEGER DEFAULT 10,
    sort_order INTEGER DEFAULT 0
);

CREATE TABLE judging.judge_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    judge_id UUID NOT NULL,
    submission_id UUID NOT NULL,
    batch_id UUID,
    track_id UUID,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING','IN_PROGRESS','COMPLETED','RECUSED')),
    assigned_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(event_id, judge_id, submission_id)
);

CREATE TABLE judging.scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    judge_id UUID NOT NULL,
    submission_id UUID NOT NULL,
    criterion_id UUID REFERENCES judging.criteria(id),
    raw_score INTEGER NOT NULL,
    idempotency_key VARCHAR(255) UNIQUE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(judge_id, submission_id, criterion_id)
);

CREATE TABLE judging.normalized_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    judge_id UUID NOT NULL,
    submission_id UUID NOT NULL,
    criterion_id UUID REFERENCES judging.criteria(id),
    z_score DECIMAL(10,6),
    shrinkage_adjusted_z DECIMAL(10,6),
    judge_review_count INTEGER,
    computed_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(judge_id, submission_id, criterion_id)
);

CREATE TABLE judging.final_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    submission_id UUID NOT NULL UNIQUE,
    weighted_score DECIMAL(10,6),
    display_score DECIMAL(10,4),
    rank INTEGER,
    judge_count INTEGER,
    computed_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE judging.calibration_submissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    submission_id UUID NOT NULL
);

CREATE TABLE judging.calibration_reference_scores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    calibration_submission_id UUID REFERENCES judging.calibration_submissions(id),
    criterion_id UUID REFERENCES judging.criteria(id),
    reference_score INTEGER NOT NULL
);

CREATE TABLE judging.calibration_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    judge_id UUID NOT NULL,
    calibration_submission_id UUID REFERENCES judging.calibration_submissions(id),
    criterion_id UUID REFERENCES judging.criteria(id),
    judge_score INTEGER NOT NULL,
    reference_score INTEGER NOT NULL,
    deviation INTEGER NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE judging.conflict_of_interest (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    judge_id UUID NOT NULL,
    submission_id UUID NOT NULL,
    reason TEXT,
    declared_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(judge_id, submission_id)
);

CREATE TABLE judging.submission_flags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    judge_id UUID NOT NULL,
    submission_id UUID NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING','REVIEWED','DISMISSED')),
    flagged_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_scores_judge ON judging.scores(judge_id);
CREATE INDEX idx_scores_submission ON judging.scores(submission_id);
CREATE INDEX idx_scores_event ON judging.scores(event_id);
CREATE INDEX idx_assignments_judge ON judging.judge_assignments(judge_id);
CREATE INDEX idx_assignments_event ON judging.judge_assignments(event_id);
CREATE INDEX idx_final_scores_event ON judging.final_scores(event_id);
CREATE INDEX idx_coi_judge ON judging.conflict_of_interest(judge_id);
""",
    "src/main/resources/db/migration/V2__enable_rls.sql": """
ALTER TABLE judging.scores ENABLE ROW LEVEL SECURITY;
ALTER TABLE judging.scores FORCE ROW LEVEL SECURITY;

CREATE POLICY judge_scores_select ON judging.scores
    FOR SELECT USING (
        judge_id = current_setting('app.current_judge_id', true)::uuid
        OR current_setting('app.current_role', true) IN ('ORGANIZER', 'ADMIN')
    );

CREATE POLICY judge_scores_insert ON judging.scores
    FOR INSERT WITH CHECK (
        judge_id = current_setting('app.current_judge_id', true)::uuid
    );

CREATE POLICY judge_scores_update ON judging.scores
    FOR UPDATE USING (
        judge_id = current_setting('app.current_judge_id', true)::uuid
    );

ALTER TABLE judging.normalized_scores ENABLE ROW LEVEL SECURITY;
ALTER TABLE judging.normalized_scores FORCE ROW LEVEL SECURITY;

CREATE POLICY judge_normalized_select ON judging.normalized_scores
    FOR SELECT USING (
        judge_id = current_setting('app.current_judge_id', true)::uuid
        OR current_setting('app.current_role', true) IN ('ORGANIZER', 'ADMIN')
    );
""",
    "src/main/java/com/dogfood/judging/Application.java": """package com.dogfood.judging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.dogfood.judging", "com.dogfood.common"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
""",
    "src/main/java/com/dogfood/judging/security/SecurityConfig.java": """package com.dogfood.judging.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
""",
    "src/main/java/com/dogfood/judging/security/RlsContextFilter.java": """package com.dogfood.judging.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class RlsContextFilter implements Filter {

    private static final ThreadLocal<String> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentRole = new ThreadLocal<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String userId = req.getHeader("X-User-Id");
        String roles = req.getHeader("X-User-Roles");

        if (userId != null) {
            currentUserId.set(userId);
        }
        if (roles != null) {
            currentRole.set(roles); // Simplified for this demo
        }

        try {
            chain.doFilter(request, response);
        } finally {
            currentUserId.remove();
            currentRole.remove();
        }
    }

    public static String getCurrentUserId() {
        return currentUserId.get();
    }

    public static String getCurrentRole() {
        return currentRole.get();
    }
}
""",
    "src/main/java/com/dogfood/judging/security/RlsAwareTransactionManager.java": """package com.dogfood.judging.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Configuration
public class RlsAwareTransactionManager {

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf, DataSource dataSource) {
        return new JpaTransactionManager(emf) {
            @Override
            protected void doBegin(Object transaction, TransactionDefinition definition) {
                super.doBegin(transaction, definition);
                String userId = RlsContextFilter.getCurrentUserId();
                String role = RlsContextFilter.getCurrentRole();

                if (userId != null || role != null) {
                    try {
                        Connection conn = getDataSource().getConnection();
                        try (Statement stmt = conn.createStatement()) {
                            if (userId != null) {
                                stmt.execute("SELECT set_config('app.current_judge_id', '" + userId + "', true)");
                            }
                            if (role != null) {
                                stmt.execute("SELECT set_config('app.current_role', '" + role + "', true)");
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Failed to set RLS variables", e);
                    }
                }
            }
        };
    }
}
"""
}

for path, content in files.items():
    full_path = os.path.join(base_dir, path)
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, "w") as f:
        f.write(content)
