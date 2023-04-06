import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.8.20" apply false
    java
    `maven-publish`
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(
            group = "org.jetbrains.kotlin",
            name = "kotlin-gradle-plugin",
            version = "1.8.20"
        )
    }
}

allprojects {
    version = getVersionFromGit()
    group = "net.nmandery.keo"

    repositories {
        mavenCentral()
    }
}

fun getVersionFromGit(fallback: String = "unknown"): String {
    return try {
        ByteArrayOutputStream().use { os ->
            exec {
                commandLine("git", "describe", "--dirty", "--always")
                standardOutput = os
            }
            os.toString("UTF8").lines().firstOrNull() ?: "unknown"
        }
            .run {
                if ("^v[0-9].".toRegex().containsMatchIn(this)) {
                    this.trimStart('v')
                } else {
                    this
                }
            }
            // replace "-" so this is not understood as a classifier of the java package
            .replace("-", "_")
    } catch (e: Exception) {
        println("not build from a git repository")
        fallback
    }.also {
        println("${project.name} version: $it")
    }
}


subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "kotlin")
    dependencies {
        implementation(kotlin("stdlib-jdk8", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))
        implementation(kotlin("reflect", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))
        testImplementation(kotlin("test", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))
        testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")

        implementation(group = "org.locationtech.jts", name = "jts-core", version = "1.19.0")
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    }
    tasks.withType<Test>() {
        useJUnitPlatform()
        testLogging {
            // show standard out and standard error of the test JVM(s) on the console
            showStandardStreams = true
            events("passed", "skipped", "failed", "standard_out", "standard_error")
        }
    }
    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_11
    }
    java {
        withSourcesJar()
    }
    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/nmandery/keo")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("GH_USERNAME")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("GH_TOKEN")
                }
            }
        }
        publications {
            create<MavenPublication>("gpr") {
                from(components["java"])
            }
        }
    }
}
