val jSoupVersion = "1.14.3"

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jsoup:jsoup:$jSoupVersion")
}
