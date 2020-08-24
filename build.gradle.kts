import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.tree-ware"
version = "1.0-SNAPSHOT"

val kotlinVersion = "1.3.72"

val jsonVersion = "1.1.4"

val log4j2Version = "2.12.1"

val junitVersion = "5.4.2"
val kotlinCoroutinesVersion = "1.2.2"
val mockkVersion = "1.10.0"

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.72")
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    implementation("javax.json:javax.json-api:$jsonVersion")
    implementation("org.glassfish:javax.json:$jsonVersion")

    implementation("org.apache.logging.log4j:log4j-api:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-core:$log4j2Version")

    testFixturesImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVersion")

    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinCoroutinesVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
