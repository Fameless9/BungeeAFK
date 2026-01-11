group = "net.fameless"
version = "2.6.5"

repositories {
    mavenCentral()
}

dependencies {
    api("io.netty:netty-buffer:4.2.8.Final")
    api("io.netty:netty-transport:4.2.8.Final")
    api("io.netty:netty-codec:4.2.8.Final")
    implementation(libs.gson)
}


