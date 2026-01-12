plugins {
    id("com.gradleup.shadow") version "9.3.0"
}

description = "BungeeAFK Tracking plugin compatible with Limbo servers"

repositories {
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
    archiveVersion.set(version.toString())

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

