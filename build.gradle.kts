import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.4.20" apply false
    java
    `maven-publish`
}

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath(
            group = "org.jetbrains.kotlin",
            name = "kotlin-gradle-plugin",
            version = "1.4.20"
        )
    }
}

allprojects {
    version = getCommitFromGit()
    group = "net.nmandery.keo"

    repositories {
        mavenCentral()
        jcenter()
    }
}

fun getCommitFromGit(fallback: String = "unknown"): String {
    return try {
        val commit = ByteArrayOutputStream().use { os ->
            exec {
                commandLine("git", "show", "-s", "--format=%h")
                standardOutput = os
            }
            os.toString("UTF8").lines().firstOrNull() ?: "unknown"
        }
        val isDirty = ByteArrayOutputStream().use { os ->
            exec {
                commandLine("git", "describe", "--dirty", "--always")
                standardOutput = os
            }
            os.toString("UTF8").lines().firstOrNull()?.endsWith("-dirty") ?: false
        }
        if (isDirty) {
            "${commit}_dirty"
        } else {
            commit
        }
    } catch (e: Exception) {
        println("not build from a git repository: ${e.message}")
        fallback
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
        testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.2")

        implementation(group = "org.locationtech.jts", name = "jts-core", version = "1.17.1")
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
    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_11
    }
    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/nmandery/keo")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
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
