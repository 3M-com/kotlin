import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  id("org.jetbrains.dokka")

  signing
  `maven-publish`
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))

  testImplementation("org.junit.jupiter:junit-jupiter-api:5.1.1")
  testRuntime("org.junit.jupiter:junit-jupiter-engine:5.1.1")
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}

tasks.dokka {
  outputFormat = "html"
  outputDirectory = "$buildDir/javadoc"
}

tasks.register("sourcesJar", Jar::class) {
  group = "build"
  description = "Assembles Kotlin sources"

  archiveClassifier.set("sources")
  from(sourceSets.main.get().allSource)
  dependsOn(tasks.classes)
}

tasks.register("dokkaJar", Jar::class) {
  group = "documentation"
  description = "Assembles Kotlin docs with Dokka"

  archiveClassifier.set("javadoc")
  from(tasks.dokka)
  dependsOn(tasks.dokka)
}

signing {
  setRequired(provider { gradle.taskGraph.hasTask("release") })
  sign(publishing.publications)
}

publishing {
  publications {
    create<MavenPublication>("default") {
      from(components["java"])
      artifact(tasks["sourcesJar"])
      artifact(tasks["dokkaJar"])

      pom {
        name.set(project.name)
        description.set("Kotlin compiler plugin to enable power assertions in the Kotlin programming language")
        url.set("https://github.com/bnorm/kotlin-power-assert")

        licenses {
          license {
            name.set("Apache License 2.0")
            url.set("https://github.com/bnorm/kotlin-power-assert/blob/master/LICENSE.txt")
          }
        }
        scm {
          url.set("https://github.com/bnorm/kotlin-power-assert")
          connection.set("scm:git:git://github.com/bnorm/kotlin-power-assert.git")
        }
        developers {
          developer {
            name.set("Brian Norman")
            url.set("https://github.com/bnorm")
          }
        }
      }
    }
  }

  repositories {
    if (
      hasProperty("sonatypeUsername") &&
      hasProperty("sonatypePassword") &&
      hasProperty("sonatypeSnapshotUrl") &&
      hasProperty("sonatypeReleaseUrl")
    ) {
      maven {
        val url = when {
          "SNAPSHOT" in version.toString() -> property("sonatypeSnapshotUrl")
          else -> property("sonatypeReleaseUrl")
        } as String
        setUrl(url)
        credentials {
          username = property("sonatypeUsername") as String
          password = property("sonatypePassword") as String
        }
      }
    }
    maven {
      name = "test"
      setUrl("file://${rootProject.buildDir}/localMaven")
    }
  }
}
