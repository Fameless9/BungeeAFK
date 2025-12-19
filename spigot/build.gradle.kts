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
    implementation(project(":bungeeafk-core"))
    implementation(project(":bungeeafk-api"))
    compileOnly(libs.spigot)
    implementation(libs.adventureTextMinimessage)
    implementation(libs.annotations)
    implementation(libs.adventureBukkit)
    implementation(libs.guice)
    implementation(libs.bstats)
}

group = "net.fameless"
version = "2.6.0"
description = "BungeeAFK for Spigot servers"
java.sourceCompatibility = JavaVersion.VERSION_21

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        exclude("META-INF/LICENSE*")
        exclude("META-INF/NOTICE*")

        archiveBaseName.set("BungeeAFK-Spigot")
        archiveClassifier.set("")
        archiveVersion.set("2.6.0")

        relocate("org.bstats", "net.fameless.bungeeafk.bstats")
    }

    jar {
        enabled = false
    }

    register("checkCompatibility") {
        group = "compatibility-check"
        description = "Checks compatibility for all supported Spigot versions by compiling against them."
        dependsOn(mcVersions.map { "compileAgainstSpigot$it".replace(".", "_") })
    }
}

val mcVersions = listOf(
    "1.8.8",
    "1.9.4",
    "1.10.2",
    "1.11.2",
    "1.12.2",
    "1.13.2",
    "1.14.4",
    "1.15.2",
    "1.16.5",
    "1.17.1",
    "1.18.2",
    "1.19.4",
    "1.20.4"
)

mcVersions.forEach { version ->
    val taskName = "compileAgainstSpigot$version".replace(".", "_")

    val spigotApi = "org.spigotmc:spigot-api:$version-R0.1-SNAPSHOT"

    configurations.create(taskName + "CompileOnly")
    dependencies {
        add(taskName + "CompileOnly", spigotApi)
    }

    tasks.register<JavaCompile>(taskName) {
        group = "compatibility-check"
        description = "Compile against Spigot $version"
        classpath = configurations.getByName(taskName + "CompileOnly") + sourceSets["main"].compileClasspath
        source = sourceSets["main"].java
        destinationDirectory.set(file("build/compat/$version"))
    }
}

