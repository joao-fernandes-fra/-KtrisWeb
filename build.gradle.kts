plugins {
    kotlin("multiplatform") version "2.3.0"
}

group = "com.github.joao-fernandes-fra"
version = "1.0.0"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

kotlin {
    js(IR) {
        browser {
            webpackTask {
                mainOutputFileName = "KtrisWebKt.js"
            }
            binaries.executable()
        }
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation("com.github.joao-fernandes-fra.Ktris:Ktris:v0.6.2-SNAPSHOT")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.10.2")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}