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
    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))
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

repositories {
    mavenCentral()
    maven {
        name = "ModMaven"
        url = uri("https://modmaven.dev/")
    }
}

dependencies {
    minecraft("net.minecraftforge:forge:1.20.1-47.1.0")
    implementation(project(":mcmt-core"))

    compileOnly("mekanism:Mekanism:1.20.1-10.+")
    compileOnly("mekanism:Mekanism:1.20.1-10.+:api")

    
    compileOnly(files("libs/MekanismExtras-1.20.1-1.+.jar"))
} 

sourceSets {
    main {
        java {
            srcDir("../mcmt-core/src/main/java")
        }
        resources {
            srcDir("../mcmt-core/src/main/resources")
        }
    }
}

tasks {
    build {
        dependsOn("reobfJar")
    }
}
