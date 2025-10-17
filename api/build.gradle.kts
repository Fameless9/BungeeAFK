plugins {
    `java-library`
    `maven-publish`
}

group = "net.fameless"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")
    compileOnly("com.google.code.gson:gson:2.10.1")
    compileOnly("net.kyori:adventure-text-minimessage:4.14.0")
    compileOnly("net.kyori:adventure-text-serializer-legacy:4.14.0")
    api("com.google.guava:guava:33.4.8-jre")
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                properties.put("maven.compiler.source", "17")
                properties.put("maven.compiler.target", "17")
            }
        }
    }
    repositories {
        mavenLocal()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}