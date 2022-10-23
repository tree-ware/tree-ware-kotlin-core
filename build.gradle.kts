group = "org.tree-ware"
version = "1.0-SNAPSHOT"

val jbcryptVersion = "0.4"
val jsonVersion = "1.1.4"
val kotlinxCoroutinesVersion = "1.6.2"
val loggingVersion = "1.1.1"
val mockkVersion = "1.12.0"
val okioVersion = "3.2.0"
val semverVersion = "1.3.3"

plugins {
    kotlin("multiplatform") version "1.7.0"
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
                api("io.github.z4kn4fein:semver:$semverVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
                api("org.lighthousegames:logging:$loggingVersion")
                api("com.squareup.okio:okio:$okioVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":tree-ware-kotlin-core:test-fixtures"))
                implementation("io.mockk:mockk-common:$mockkVersion")
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("javax.json:javax.json-api:$jsonVersion")
                implementation("org.glassfish:javax.json:$jsonVersion")
                implementation("org.mindrot:jbcrypt:$jbcryptVersion")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(project(":tree-ware-kotlin-core:test-fixtures"))
                implementation("io.mockk:mockk:$mockkVersion")
            }
        }
    }
}