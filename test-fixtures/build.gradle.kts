group = "org.tree-ware"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("multiplatform")
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
                implementation(project(":tree-ware-kotlin-core"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(project(":tree-ware-kotlin-core"))
                implementation(kotlin("test"))
            }
        }
    }
}