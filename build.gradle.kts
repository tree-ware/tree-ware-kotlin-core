group = "org.tree-ware"
version = "1.0-SNAPSHOT"

val kotlinVersion = "1.3.40"

val log4j2Version = "2.12.1"

val junitVersion = "5.4.2"
val assertKVersion = "0.20"

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

    implementation("org.apache.logging.log4j:log4j-api:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-core:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")

    testCompile("com.willowtreeapps.assertk:assertk-jvm:$assertKVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
