import org.gradle.kotlin.dsl.version

plugins {
    id("dev.kikugie.stonecutter")

    val modstitchVersion = "0.8.4"
    id("dev.isxander.modstitch.base") version modstitchVersion apply false
    id("fabric-loom") version "1.17-SNAPSHOT" apply false
    id("net.neoforged.moddev") version "2.0.120" apply false

    id("me.modmuss50.mod-publish-plugin") version "0.8.4" apply false
}
stonecutter active "26.1-fabric"

//stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
//    group = "project"
//    ofTask("build")
//}
//
//stonecutter registerChiseled tasks.register("chiseledBuildAndCollect", stonecutter.chiseled) {
//    group = "project"
//    ofTask("buildAndCollect")
//}
//
//stonecutter registerChiseled tasks.register("chiseledPublishMods", stonecutter.chiseled) {
//    group = "project"
//    ofTask("publishMods")
//}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.terraformersmc.com/releases/")
        maven("https://maven.isxander.dev/releases/")
        maven("https://thedarkcolour.github.io/KotlinForForge/")
        maven("https://maven.quiltmc.org/repository/release/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.saps.dev/releases")
        maven("https://api.modrinth.com/maven")
        maven("https://maven.nucleoid.xyz/")
        maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
    }
}