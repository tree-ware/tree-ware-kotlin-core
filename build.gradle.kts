group = "org.tree-ware"
version = "1.0-SNAPSHOT"

val kotlinVersion = "1.3.40"

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.40")
    id("idea")
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}
