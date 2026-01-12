plugins {
    id("com.gradleup.shadow") version "9.3.0"
}

description = "BungeeAFK for Velocity proxies"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.maven.apache.org/maven2/")
}

dependencies {
    implementation(project(":bungeeafk-core"))
    implementation(project(":bungeeafk-api"))
    compileOnly(libs.velocity)
    implementation(libs.annotations)
    implementation(libs.snakeYaml)
    implementation(libs.adventureTextMinimessage)
    implementation(libs.bstatsVelocity)
}


tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveBaseName.set("BungeeAFK-Velocity")
        archiveClassifier.set("")
        archiveVersion.set(version.toString())

        relocate("org.bstats", "net.fameless.bungeeafk.bstats")
    }

    jar {
        enabled = false
    }
}
