plugins {
    kotlin("jvm") version "1.4.10" apply false
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
            version = "1.4.10"
        )
    }
}


allprojects {
    version = "1.1-SNAPSHOT"
    group = "net.nmandery.keo"

    repositories {
        mavenCentral()
        jcenter()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    dependencies {
        implementation(kotlin("stdlib-jdk8", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))
        implementation(kotlin("reflect", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))
        testImplementation(kotlin("test", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))
        testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.2")

        implementation(group = "org.locationtech.jts", name = "jts-core", version = "1.17.1")
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
        kotlinOptions.jvmTarget = "1.8"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
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
