plugins {
    java
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
    jacoco
}

group = "com.anonymouschat"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // 웹 API 구성
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring Security + OAuth2 로그인
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    // JWT
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // JPA + H2
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")

    // QueryDSL
    implementation ("io.github.openfeign.querydsl:querydsl-jpa:7.0")
    annotationProcessor ("io.github.openfeign.querydsl:querydsl-apt:7.0:jpa")
    annotationProcessor ("jakarta.persistence:jakarta.persistence-api")
    annotationProcessor ("jakarta.annotation:jakarta.annotation-api")

    // 유효성 검증
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")
    implementation("org.apache.commons:commons-lang3:3.18.0")

    // Lombok
    implementation("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // WebSocket + STOMP 지원
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    // SockJS fallback
    implementation("org.webjars:sockjs-client:1.5.1")
    implementation("org.webjars:stomp-websocket:2.3.4")

    // Thymeleaf
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // 테스트용 WebSocket 클라이언트
    testImplementation("org.springframework:spring-messaging")

    // 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
}

val querydslDir = layout.buildDirectory.dir("generated/querydsl").get().asFile

sourceSets.main {
    java.srcDir(querydslDir)
}

tasks.withType<JavaCompile>().configureEach {
    options.generatedSourceOutputDirectory.set(file("build/generated/querydsl"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
    }
}
