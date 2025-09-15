import com.anahoret.gradle.plugin.metrics.CodeMetricsPlugin
import com.anahoret.gradle.plugin.metrics.CodeMetricsPluginExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val arrowKtVersion = "2.1.2"
val cucumberVersion = "7.28.2"
val googleAuthVersion = "1.39.0"
val googleSheetsApiVersion = "v4-rev20250616-2.0.0"
val javaJwtVersion = "4.5.0"
val mockitoVersion = "5.19.0"
val mockkVersion = "1.14.5"
val pdfBoxVersion = "3.0.5"
val springDocVersion = "2.8.13"
val springmockkVersion = "4.0.2"
val testcontainersVersion = "1.21.3"
val shedlockVersion = "6.10.0"
val springAiVersion = "0.8.1"
val commonsLangVersion = "3.18.0"

plugins {
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("jacoco")
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.allopen") version "2.2.20"
    kotlin("plugin.spring") version "2.2.20"
    kotlin("plugin.jpa") version "2.2.20"
    kotlin("kapt") version "2.2.20"
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

group = "com.anahoret"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Utils
    implementation("io.arrow-kt:arrow-core:$arrowKtVersion")
    implementation("org.apache.pdfbox:pdfbox:$pdfBoxVersion") {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation("org.apache.commons:commons-lang3:$commonsLangVersion")

    // Web
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.auth0:java-jwt:$javaJwtVersion")

    // Database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.flywaydb:flyway-core")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Email
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // Google
    implementation("com.google.apis:google-api-services-sheets:$googleSheetsApiVersion") {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation("com.google.auth:google-auth-library-oauth2-http:$googleAuthVersion")

    // ShedLock
    implementation("net.javacrumbs.shedlock:shedlock-spring:$shedlockVersion")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:$shedlockVersion")

    // Development
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocVersion")
    implementation("org.springdoc:springdoc-openapi-starter-common:$springDocVersion")

    // Open AI
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("io.cucumber:cucumber-java")
    testImplementation("io.cucumber:cucumber-junit")
    testImplementation("io.cucumber:cucumber-spring")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    implementation("com.ninja-squad:springmockk:$springmockkVersion")

}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:$testcontainersVersion")
        mavenBom("io.cucumber:cucumber-bom:$cucumberVersion")
        mavenBom("org.springframework.ai:spring-ai-bom:$springAiVersion")
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("standardOut", "passed", "skipped", "failed")
    }
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

apply<CodeMetricsPlugin>()

configure<CodeMetricsPluginExtension> {
    projectId.set(178)
}
