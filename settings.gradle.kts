pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()

        // Modstitch, YACL
        maven("https://maven.isxander.dev/releases/")

        // Loom platform
        maven("https://maven.fabricmc.net/")

        // MDG platform
        maven("https://maven.neoforged.net/releases/")

        // Stonecutter
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")

        // Modstitch
        maven("https://maven.isxander.dev/releases")

        // Terraformers (Fabric, ModMenu)
        maven ("https://maven.terraformersmc.com/releases/")

        // Kotlin for Forge (YACL dependencies)
        maven ("https://thedarkcolour.github.io/KotlinForForge/")

        // Architectury
        maven ("https://maven.architectury.dev/")

        // Armor Poser
        maven ("https://maven.saps.dev/releases")

        // Modrinth (SkinShuffle)
        maven ("https://api.modrinth.com/maven")

        // Placeholder API
        maven("https://maven.nucleoid.xyz/")

        // Forge Config API port
        maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")

    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9"
}

//includeBuild("../modstitch")

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        /**
         * @param mcVersion The base minecraft version.
         * @param loaders A list of loaders to target, supports "fabric" (1.14+), "neoforge"(1.20.6+), "vanilla"(any) or "forge"(<=1.20.1)
         */
        fun mc(mcVersion: String, name: String = mcVersion, loaders: Iterable<String>) =
            loaders.forEach { version("$name-$it", mcVersion) }

        // Configure your targets here!
        mc("26.2", loaders = listOf("fabric"))
        mc("26.1", loaders = listOf("fabric"))
//        mc("1.21.1", loaders = listOf("fabric", "neoforge"))

        // This is the default target.
        // https://stonecutter.kikugie.dev/stonecutter/guide/setup#settings-settings-gradle-kts
        vcsVersion = "26.1-fabric"
    }
}

rootProject.name = "Player Armor Stands"

