plugins {
    id("com.gradleup.shadow") version "9.3.0"
}

group = "net.fameless"
version = "2.6.5"
description = "BungeeAFK Tracking plugin for Limbo"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
    maven("https://repo.loohpjames.com/repository")
}

dependencies {
    implementation(project(":bungeeafk-network"))
    implementation(libs.snakeYaml)
    compileOnly("com.loohp:Limbo:0.7.18-ALPHA")
}

tasks.shadowJar {
    archiveBaseName.set("BungeeAFK-Limbo-Tracking")
    archiveClassifier.set("")
    archiveVersion.set("2.6.5")

    relocate("io.netty", "net.fameless.libs.netty")
    exclude("META-INF/**")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false
    }
}

