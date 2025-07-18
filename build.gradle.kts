plugins {
    java
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
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
    // 웹 API 구성 (REST Controller 등)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring Security + OAuth2 로그인
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    // JWT 토큰 생성 및 검증용 라이브러리 (com.auth0)
    implementation("com.auth0:java-jwt:4.4.0")

    // JPA + MySQL 연동
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.mysql:mysql-connector-j")

    // 유효성 검증 (@Valid, @NotBlank 등)
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Swagger UI (API 문서 자동화)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")
    implementation("org.apache.commons:commons-lang3:3.18.0")

    // 테스트 라이브러리
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
