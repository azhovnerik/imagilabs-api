import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val arrowKtVersion = "1.1.5"
val cucumberVersion = "7.12.0"
val googleAuthVersion = "1.16.0"
val googleSheetsApiVersion = "v4-rev612-1.25.0"
val javaJwtVersion = "4.4.0"
val mockitoVersion = "5.2.0"
val mockkVersion = "1.13.4"
val pdfBoxVersion = "2.0.28"
val springDocVersion = "1.7.0"
val springmockkVersion = "4.0.2"
val testcontainersVersion = "1.18.1"


plugins {
    id("org.springframework.boot") version "3.0.6"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.allopen") version "1.8.21"
    kotlin("plugin.spring") version "1.8.21"
    kotlin("plugin.jpa") version "1.8.21"
    kotlin("kapt") version "1.8.21"
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

group = "com.anahoret"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springdoc:springdoc-openapi-kotlin")

    // Utils
    implementation("io.arrow-kt:arrow-core:$arrowKtVersion")
    implementation("org.apache.pdfbox:pdfbox:$pdfBoxVersion")

    // Web
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.auth0:java-jwt:$javaJwtVersion")

    // Database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Email
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // Google
    implementation("com.google.apis:google-api-services-sheets:$googleSheetsApiVersion")
    implementation("com.google.auth:google-auth-library-oauth2-http:$googleAuthVersion")

    // Development
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springdoc:springdoc-openapi-ui")
    implementation("org.springdoc:springdoc-openapi-security")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
//    testImplementation("org.junit.vintage:junit-vintage-engine")
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
        mavenBom("org.springdoc:springdoc-openapi:$springDocVersion")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
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
