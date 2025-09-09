// 1. Eerst pluginManagement
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.minecraftforge.net")
    }
}

// 2. Dan dependencyResolutionManagement
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenCentral()
        maven("https://maven.minecraftforge.net")
        // Voeg hier extra maven(...) toe als je externe libs nodig hebt
    }
}

// 3. Daarna pas projectnaam en includes
rootProject.name = "mcmt-forge-1.20.1"

// Subprojecten opnemen
include("mcmt-core", "mcmt-forge")

// Eventueel expliciet de mappen aanwijzen
project(":mcmt-core").projectDir = file("mcmt-core")
project(":mcmt-forge").projectDir = file("mcmt-forge")
