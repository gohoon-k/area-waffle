plugins {
    java
    kotlin("jvm") version "1.4.32"
    id("idea")
    id("com.github.johnrengelman.shadow") version "2.0.4"
}

group = "io.gohoon.waffle"
version = "1.2.7-b176-m1.17.1"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    
    implementation(kotlin("stdlib"))
    testImplementation("junit", "junit", "4.12")
}
