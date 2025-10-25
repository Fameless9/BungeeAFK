plugins {
    `java-library`
    `maven-publish`
    id("com.diffplug.spotless") version "7.0.0.BETA1"
}

group = "net.fameless"
version = "2.5.0"
description = "BungeeAFK"
java.sourceCompatibility = JavaVersion.VERSION_21

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.diffplug.spotless")

    group = "net.fameless"
    version = "2.5.0"

    repositories {
        mavenCentral()
    }

    spotless {
        java {
            target("**/*.java")
            removeUnusedImports()
            toggleOffOn()
            trimTrailingWhitespace()
            endWithNewline()
            formatAnnotations()
            indentWithSpaces(4)
        }
    }

    tasks {
        build {
            dependsOn(spotlessApply)
        }
    }
}
