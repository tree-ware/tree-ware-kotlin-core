import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.tree-ware"
version = "1.0-SNAPSHOT"

val kotlinCoroutinesVersion = "1.5.0"

val jsonVersion = "1.1.4"

val log4j2Version = "2.14.1"

val mockkVersion = "1.12.0"

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.5.21")
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

    implementation("javax.json:javax.json-api:$jsonVersion")
    implementation("org.glassfish:javax.json:$jsonVersion")

    implementation("org.apache.logging.log4j:log4j-api:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-core:$log4j2Version")

    testFixturesImplementation(kotlin("test"))
    testImplementation(kotlin("test"))

    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinCoroutinesVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

tasks.test {
    useJUnitPlatform()
}
