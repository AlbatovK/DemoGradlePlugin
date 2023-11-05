plugins {
    kotlin("jvm") version "1.9.0"
    id("gradle-plugin")
    application
}

group = "org.example"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

task("clear") {
    project.buildDir.delete()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}