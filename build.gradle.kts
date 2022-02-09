group = "org.tree-ware"
version = "1.0-SNAPSHOT"

val jbcryptVersion = "0.4"
val jsonVersion = "1.1.4"
val kotlinxCoroutinesVersion = "1.6.0"
val loggingVersion = "1.1.1"
val mockkVersion = "1.12.0"

plugins {
    kotlin("multiplatform") version "1.6.10"
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
            useJUnitPlatform()
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
                implementation("org.lighthousegames:logging:$loggingVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":tree-ware-kotlin-core:test-fixtures"))
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