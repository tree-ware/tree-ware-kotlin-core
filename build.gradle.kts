// The libraries are currently published to JitPack. JitPack picks up the
// version from the repo label, resulting in all libraries from the repo
// having the same version in JitPack. Setting the version for all projects
// conveys this.
allprojects {
    group = "org.tree-ware.tree-ware-kotlin-core"
    version = "0.1.0.3"
}

val jbcryptVersion = "0.4"
val jsonVersion = "1.1.4"
val kotlinxBenchmarkVersion = "0.4.4"
val kotlinxCoroutinesVersion = "1.6.2"
val loggingVersion = "1.1.1"
val mockkVersion = "1.12.0"
val okioVersion = "3.2.0"
val semverVersion = "1.3.3"

plugins {
    kotlin("multiplatform") version "1.7.0"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.4"
    id("maven-publish")
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations {
            create("benchmarks")
            all {
                kotlinOptions.jvmTarget = "1.8"
            }
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
                implementation(project(":test-fixtures"))
                implementation("io.mockk:mockk-common:$mockkVersion")
                implementation(kotlin("test"))
            }
        }
        val commonBenchmarks by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:$kotlinxBenchmarkVersion")
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
                implementation(project(":test-fixtures"))
                implementation("io.mockk:mockk:$mockkVersion")
            }
        }
        val jvmBenchmarks by getting {
            dependsOn(commonBenchmarks)
            dependsOn(jvmMain)
        }
    }
}

benchmark {
    targets {
        register("jvmBenchmarks")
    }
}