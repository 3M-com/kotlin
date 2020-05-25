plugins {
  kotlin("jvm") version "1.4-M1" apply false
  id("org.jetbrains.dokka") version "0.10.0" apply false
  id("com.gradle.plugin-publish") version "0.11.0" apply false
}

allprojects {
  group = "com.bnorm.power"
  version = "0.4.0-SNAPSHOT"
}

subprojects {
  repositories {
    mavenCentral()
    jcenter()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
  }
}
