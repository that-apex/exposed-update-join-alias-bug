plugins {
    kotlin("jvm") version "2.0.0"
    application
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.exposed:exposed-core:0.53.0")
    runtimeOnly("org.jetbrains.exposed:exposed-jdbc:0.53.0")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client:3.4.1")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass = "com.example.MainKt"
}
