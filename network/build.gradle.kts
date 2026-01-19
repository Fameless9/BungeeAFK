description = "BungeeAFK network library for communication between proxy and server plugins"

dependencies {
    api("io.netty:netty-buffer:4.2.8.Final")
    api("io.netty:netty-transport:4.2.8.Final")
    api("io.netty:netty-codec:4.2.8.Final")
    compileOnly(libs.annotations)
    implementation(libs.gson)
}


