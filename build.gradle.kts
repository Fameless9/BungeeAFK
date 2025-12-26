plugins {
    `java-library`
    `maven-publish`
    id("com.diffplug.spotless") version "8.1.0"
}

group = "net.fameless"
version = "2.6.0"
description = "BungeeAFK"
java.sourceCompatibility = JavaVersion.VERSION_21

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.diffplug.spotless")

    group = "net.fameless"
    version = "2.6.0"

    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    spotless {
        java {
            target("**/*.java")
            removeUnusedImports()
            toggleOffOn()
            trimTrailingWhitespace()
            endWithNewline()
            formatAnnotations()
            leadingTabsToSpaces(4)
        }
    }

    tasks {
        build {
            dependsOn(spotlessApply)
        }
    }
}
