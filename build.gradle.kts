import kotlinx.benchmark.gradle.JvmBenchmarkTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.tree-ware"
version = "1.0-SNAPSHOT"

val bouncyCastleVersion = "1.69"
val kotlinBenchmarkVersion = "0.3.1"
val kotlinCoroutinesVersion = "1.5.0"
val jbcryptVersion = "0.4"
val jsonVersion = "1.1.4"
val log4j2Version = "2.16.0"
val mockkVersion = "1.12.0"

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.5.21")
    id("org.jetbrains.kotlin.plugin.allopen").version("1.5.21") // for benchmarks, to keep JMH happy
    id("org.jetbrains.kotlinx.benchmark").version("0.3.1") // $kotlinBenchmarkVersion
    id("idea")
    id("java-library")
    id("java-test-fixtures")
}

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile> {
    // Compile for Java 8 (default is Java 6)
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("org.bouncycastle:bcpkix-jdk15on:$bouncyCastleVersion")
    implementation("org.mindrot:jbcrypt:$jbcryptVersion")
    implementation("javax.json:javax.json-api:$jsonVersion")
    implementation("org.glassfish:javax.json:$jsonVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinCoroutinesVersion")

    implementation("org.apache.logging.log4j:log4j-api:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-core:$log4j2Version")

    testFixturesImplementation(kotlin("test"))
    testImplementation(kotlin("test"))

    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

tasks.test {
    useJUnitPlatform()
}

// Benchmarks

// Create a separate source set for benchmarks.
sourceSets.create("benchmarks")

kotlin.sourceSets.getByName("benchmarks") {
    dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:$kotlinBenchmarkVersion")

        implementation(sourceSets.main.get().output)
        implementation(sourceSets.main.get().runtimeClasspath)
    }
}

benchmark {
    configurations {
        named("main") {
            // configure default configuration
        }
    }
    targets {
        register("benchmarks") {
            this as JvmBenchmarkTarget
            jmhVersion = "1.21"
        }
    }
}

// For benchmarks, to keep JMH happy
allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}