val log4j2Version = "2.19.0"
val mockkVersion = "1.13.16"

plugins {
    kotlin("multiplatform") version "2.1.10"
    id("maven-publish")
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
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
        commonMain.dependencies {
            implementation(project(":"))
            implementation("io.mockk:mockk:$mockkVersion")
            implementation(kotlin("test"))
        }
        jvmMain.dependencies {
            implementation("io.mockk:mockk:$mockkVersion")
            implementation("org.apache.logging.log4j:log4j-core:${log4j2Version}")
            implementation("org.apache.logging.log4j:log4j-slf4j2-impl:${log4j2Version}")
        }
    }
}