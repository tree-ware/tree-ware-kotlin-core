// The libraries are currently published to JitPack. JitPack picks up the
// version from the repo label, resulting in all libraries from the repo
// having the same version in JitPack. Setting the version for all projects
// conveys this.
allprojects {
    group = "org.tree-ware.tree-ware-kotlin-core"
    version = "0.5.1.0-SNAPSHOT"
}

val jbcryptVersion = "0.4"
val jsonVersion = "1.1.4"
val kotlinxBenchmarkVersion = "0.4.13"
val kotlinxCoroutinesVersion = "1.10.1"
val loggingVersion = "2.0.3"
val mockkVersion = "1.13.16"
val okioVersion = "3.10.2"
val semverVersion = "2.0.0"

plugins {
    kotlin("multiplatform") version "2.1.10"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.13"
    id("maven-publish")
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations {
            create("benchmarks")
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

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            api("io.github.z4kn4fein:semver:$semverVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
            api("org.lighthousegames:logging:$loggingVersion")
            api("com.squareup.okio:okio:$okioVersion")
        }
        commonTest.dependencies {
            implementation(project(":test-fixtures"))
            implementation("io.mockk:mockk:$mockkVersion")
            implementation(kotlin("test"))
        }
        val commonBenchmarks by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:$kotlinxBenchmarkVersion")
            }
        }
        jvmMain.dependencies {
            implementation("javax.json:javax.json-api:$jsonVersion")
            implementation("org.glassfish:javax.json:$jsonVersion")
            implementation("org.mindrot:jbcrypt:$jbcryptVersion")
        }
        jvmTest.dependencies {
            implementation(project(":test-fixtures"))
            implementation("io.mockk:mockk:$mockkVersion")
        }
        val jvmBenchmarks by getting {
            dependsOn(commonBenchmarks)
            dependsOn(jvmMain.get())
        }
    }
}

benchmark {
    targets {
        register("jvmBenchmarks")
    }
}