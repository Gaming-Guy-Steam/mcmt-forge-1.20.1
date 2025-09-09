plugins {
    base
    kotlin("jvm") version "1.9.10" apply false
}

subprojects {
    plugins.withId("net.minecraftforge.gradle") {
        // Zoek de 'minecraft' extensie en roep accessTransformer(...) aan via reflectie
        extensions.findByName("minecraft")?.let { mcExt ->
            val method = mcExt::class.java.methods.firstOrNull { m ->
                m.name == "accessTransformer" && m.parameterTypes.size == 1
            }
            method?.invoke(mcExt, file("${rootProject.projectDir}/src/main/resources/META-INF/accesstransformer.cfg"))
            println("Applied Access Transformer to project ${project.name}")
        }
    }
}
                   
allprojects {
    group = "com.yourname.mcmt"
    version = "1.0.0"
}
