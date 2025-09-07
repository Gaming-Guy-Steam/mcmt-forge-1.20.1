// mcmt-forge/build.gradle.kts
plugins {
    id("net.minecraftforge.gradle") version "6.0.24"
    kotlin("jvm")
    java
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

minecraft {
    mappings("official", "1.20.1")
    runs {
        create("client") {
            workingDirectory(file("run"))
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            mods {
                create("mcmt") {
                    source(sourceSets.main.get())
                }
            }
        }
    }
}

dependencies {
    minecraft("net.minecraftforge:forge:1.20.1-47.1.0")
    implementation(project(":mcmt-core"))
}

// Zorg dat 'build' ook de reobfJar uitvoert
tasks {
    build {
        dependsOn("reobfJar")
    }
}
