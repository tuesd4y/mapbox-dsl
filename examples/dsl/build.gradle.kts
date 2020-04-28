plugins {
    id("org.jetbrains.kotlin.js") version "1.3.70"
}

group = "at.triply"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-js"))
}

kotlin.target.browser { }