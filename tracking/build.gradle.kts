plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.maven.apache.org/maven2/")
}

dependencies {
    compileOnly(libs.spigot)
}

group = "net.fameless"
version = "2.5.5"
description = "Tracking plugin required by BungeeAFK"
java.sourceCompatibility = JavaVersion.VERSION_21

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveBaseName.set("BungeeAFK-Tracking")
        archiveClassifier.set("")
        archiveVersion.set("2.5.5")
    }

    jar {
        enabled = false
    }
}
