val jSoupVersion = "1.21.2"

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jsoup:jsoup:$jSoupVersion")
}
