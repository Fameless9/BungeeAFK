plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "9.0.0-beta12"
}

repositories {
    mavenCentral()
    maven("https://mvnrepository.com/artifact/net.md-5/bungeecord-api")
    mavenLocal()
}

dependencies {
    implementation(project(":bungeeafk-core"))
    implementation(project(":bungeeafk-api"))
    compileOnly(libs.bungee)
    compileOnly(libs.annotations)
    implementation(libs.guice)
    implementation(libs.adventureBungee)
    implementation(libs.adventureTextMinimessage)
    implementation(libs.bstatsBungee)
}

group = "net.fameless"
version = "2.4.1"
description = "BungeeAFK for BungeeCord proxies"
java.sourceCompatibility = JavaVersion.VERSION_21

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveBaseName.set("BungeeAFK-Bungee")
        archiveClassifier.set("")
        archiveVersion.set("2.4.1")

        relocate("org.bstats", "net.fameless.bungeeafk.bstats")
    }

    jar {
        enabled = false
    }
}
