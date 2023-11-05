plugins {
    `kotlin-dsl`
    `java-library`
    antlr
}

gradlePlugin {
    plugins {
        register("gradle-plugin") {
            id = "gradle-plugin"
            description = "Example plugin"
            displayName = "Counts the number of different code structures and LoC using antlr"
            implementationClass = "GradlePlugin"
        }
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

task("generateProjectStatistic").dependsOn("build")

dependencies {
    implementation(gradleKotlinDsl())
    implementation("org.antlr:antlr4:4.7.1")
    antlr("org.antlr:antlr4:4.7.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
    implementation("org.antlr:antlr4:4.7.1")
    implementation("org.antlr:antlr4-runtime:4.7.1")
}

