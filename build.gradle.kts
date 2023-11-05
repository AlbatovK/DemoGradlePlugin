plugins {
    kotlin("jvm") version "1.9.0"
    application
    id("gradle-plugin")
    antlr
}

group = "org.example"
version = "0.1-SNAPSHOT"

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    outputDirectory = file("buildSrc/build/generated-src")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    antlr("org.antlr:antlr4:4.7.1")
    implementation("org.antlr:antlr4:4.7.1")
    implementation("org.antlr:antlr4-runtime:4.7.1")
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