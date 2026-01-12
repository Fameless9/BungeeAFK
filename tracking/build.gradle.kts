plugins {
    id("com.gradleup.shadow") version "9.3.0"
}

description = "Tracking plugin required by BungeeAFK"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    implementation(project(":bungeeafk-network"))
    implementation(libs.adventureBukkit)
    implementation(libs.adventureTextMinimessage)
    implementation(libs.annotations)
    compileOnly(libs.placeholderApi)
    compileOnly(libs.paper)
}

tasks.shadowJar {
    archiveBaseName.set("BungeeAFK-Tracking")
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
