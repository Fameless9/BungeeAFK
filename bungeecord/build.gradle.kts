plugins {
    id("com.gradleup.shadow") version "9.3.0"
}

repositories {
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

description = "BungeeAFK for BungeeCord proxies"

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveBaseName.set("BungeeAFK-Bungee")
        archiveClassifier.set("")
        archiveVersion.set(version.toString())

        relocate("io.netty", "net.fameless.bungeeafk.netty")
        relocate("org.bstats", "net.fameless.bungeeafk.bstats")
    }

    jar {
        enabled = false
    }
}
