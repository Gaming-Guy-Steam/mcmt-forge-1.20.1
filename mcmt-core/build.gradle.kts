// mcmt-core/build.gradle.kts
plugins {
    kotlin("jvm")
    java
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

dependencies {
    // Zet hier je core dependencies
    // Bijvoorbeeld:
    // implementation("org.jetbrains.kotlin:kotlin-stdlib")
}
