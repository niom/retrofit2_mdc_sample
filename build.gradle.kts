plugins {
    kotlin("jvm") version "1.4.21"
    java
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.9.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:3.14.9")
    implementation("com.squareup.okhttp3:logging-interceptor:3.14.9")
    implementation("com.squareup.okhttp3:mockwebserver:3.14.9")

    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.slf4j:jul-to-slf4j:1.7.30")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")

    implementation("io.reactivex.rxjava2:rxjava:2.2.20")

    implementation("io.github.microutils:kotlin-logging-jvm:2.0.2")
    // implementation("org.jetbrains.kotlinx:kotlinx:1.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.4.2")
    // https://mvnrepository.com/artifact/javax.annotation/javax.annotation-api
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    // https://mvnrepository.com/artifact/javax/javaee-api
    implementation("com.google.code.findbugs:jsr305:3.0.2")


}
