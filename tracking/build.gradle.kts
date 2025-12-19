plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.0.0-beta12"
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.maven.apache.org/maven2/")
}

dependencies {
    implementation(project(":bungeeafk-network"))
    compileOnly(libs.spigot)
}

group = "net.fameless"
version = "2.6.0"
description = "Tracking plugin required by BungeeAFK"
java.sourceCompatibility = JavaVersion.VERSION_21

tasks.shadowJar {
    archiveBaseName.set("BungeeAFK-Tracking")
    archiveClassifier.set("")
    archiveVersion.set("2.6.0")

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
