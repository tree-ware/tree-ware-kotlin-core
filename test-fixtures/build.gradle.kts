group = "org.tree-ware.tree-ware-kotlin-core"
version = "0.1.0.0"

val log4j2Version = "2.16.0"
val mockkVersion = "1.12.0"

plugins {
    kotlin("multiplatform")
    id("maven-publish")
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform {
                when (System.getProperty("integrationTests", "")) {
                    "include" -> includeTags("integrationTest")
                    "exclude" -> excludeTags("integrationTest")
                    else -> {}
                }
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":"))
                implementation("io.mockk:mockk-common:$mockkVersion")
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.mockk:mockk:$mockkVersion")
                implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")
            }
        }
    }
}