plugins {
    `java-library`
    `maven-publish`
    id("com.diffplug.spotless") version "8.1.0"
}

description = "The BungeeAFK Project"

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.diffplug.spotless")

    group = "net.fameless"
    version = "2.6.5"
    java.sourceCompatibility = JavaVersion.VERSION_21

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}

subprojects {
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
            leadingTabsToSpaces(4)
        }
    }

    tasks {
        build {
            dependsOn(spotlessApply)
        }
    }
}
