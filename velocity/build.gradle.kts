plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "9.0.0-beta12"
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.maven.apache.org/maven2/")
}

dependencies {
    implementation(project(":bungeeafk-core"))
    compileOnly(libs.velocity)
    implementation(libs.annotations)
    implementation(libs.snakeYaml)
    implementation(libs.adventureTextMinimessage)
    implementation(libs.bstatsVelocity)
}

group = "net.fameless"
version = "2.3.0"
description = "BungeeAFK for Velocity proxies"
java.sourceCompatibility = JavaVersion.VERSION_21

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveBaseName.set("BungeeAFK-Velocity")
        archiveClassifier.set("")
        archiveVersion.set("2.3.0")

        relocate("org.bstats", "net.fameless.bungeeafk.bstats")
    }

    jar {
        enabled = false
    }
}
