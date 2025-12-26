group = "net.fameless"
version = "2.6.0"
description = "Core features implementing the basic logic of BungeeAFK"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":bungeeafk-api"))
    implementation(project(":bungeeafk-network"))
    compileOnly(libs.annotations)
    implementation(libs.gson)
    implementation(libs.guice)
    implementation(libs.adventureTextMinimessage)
    implementation(libs.adventureTextSerializerLegacy)
    implementation(libs.snakeYaml)
    api(libs.slf4j)
    api(libs.logback)
}
