plugins {
    kotlin("js") version "2.3.0"
}

group = "com.kari-placeholder"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.github.joao-fernandes-fra.Ktris:Ktris:v0.6.2-SNAPSHOT")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.10.2")
}

kotlin {
    js(IR) {
        browser {
            binaries.executable()
        }
        nodejs()
    }
}