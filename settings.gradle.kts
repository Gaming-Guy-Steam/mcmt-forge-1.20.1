rootProject.name = "mcmt-forge-1.20.1"

include("mcmt-core", "mcmt-forge")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.minecraftforge.net")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenCentral()
        maven("https://maven.minecraftforge.net")
        // Voeg hier extra maven(...) toe als je externe libs nodig hebt
    }
}
