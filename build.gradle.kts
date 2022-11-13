plugins {
    application
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
}

group = "dev.kason"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.http4k:http4k-core:4.33.3.0")
    implementation("org.http4k:http4k-server-jetty:4.33.3.0")
    implementation("ch.qos.logback:logback-classic:1.4.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("org.http4k:http4k-client-websocket:4.33.3.0")
    implementation("org.jetbrains.kotlin:kotlin-test-junit:1.7.10")
}