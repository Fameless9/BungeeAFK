description = "Core features implementing the basic logic of BungeeAFK"

repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":bungeeafk-api"))
    implementation(project(":bungeeafk-network"))
    compileOnly(libs.annotations)
    compileOnly(libs.tabApi)
    implementation(libs.gson)
    implementation(libs.guice)
    implementation(libs.adventureTextMinimessage)
    implementation(libs.adventureTextSerializerLegacy)
    implementation(libs.snakeYaml)
    api(libs.slf4j)
    api(libs.logback)
}
