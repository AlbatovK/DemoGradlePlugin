plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("gradle-plugin") {
            id = "gradle-plugin"
            description = "Example plugin"
            displayName = "Does nothing (for now)"
            implementationClass = "GradlePlugin"
        }
    }
}


repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleKotlinDsl())
}

