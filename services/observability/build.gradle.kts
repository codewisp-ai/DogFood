plugins {
    id("org.springframework.boot")
}
dependencies {
    implementation(project(":services:common"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.7.3")
    runtimeOnly("org.postgresql:postgresql")
}
