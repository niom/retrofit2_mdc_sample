plugins {
    kotlin("jvm") version "1.4.21"
    java
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:3.14.9")
    implementation("com.squareup.okhttp3:logging-interceptor:3.14.9")
    implementation("com.squareup.okhttp3:mockwebserver:4.9.0")

    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.slf4j:jul-to-slf4j:1.7.30")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
}
