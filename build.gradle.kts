import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.modstitch

plugins {
    id("dev.isxander.modstitch.base")
    id("me.modmuss50.mod-publish-plugin")
}

fun prop(name: String, consumer: (prop: String) -> Unit) {
    (findProperty(name) as? String?)
        ?.let(consumer)
}

val gitBranchName = ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD")
    .start()
    .inputStream
    .bufferedReader()
    .use { it.readText().trim() }
val minecraft = property("deps.minecraft") as String
val loader: String = name.split("-")[1]
val loaderInitials: String = when (loader) {
    "fabric" -> "FBR"
    "neoforge" -> "NFG"
    "forge" -> "FG"
    "vanilla" -> "VNL"
    else -> throw IllegalArgumentException("Unknown loader: $loader")
}

var isPossessive: Boolean = loader == "fabric" && findProperty("deps.possessive") != null
var isArmorposer: Boolean = stonecutter.eval(minecraft, ">=1.21.4") && findProperty("deps.armorposer") != null
var isEasyAnvils: Boolean = findProperty("deps.easyanvils") != null
var hasModMenu: Boolean = findProperty("deps.modmenu") != null
var hasYacl: Boolean = findProperty("deps.yacl") != null

modstitch {
    minecraftVersion = minecraft

    // Alternatively use stonecutter.eval if you have a lot of versions to target.
    // https://stonecutter.kikugie.dev/stonecutter/guide/setup#checking-versions
//    val j25: Boolean = stonecutter.eval(minecraft, ">=26.1")
//    javaVersion = if (j25) 25 else 21

    // If parchment doesn't exist for a version, yet you can safely
    // omit the "deps.parchment" property from your versioned gradle.properties
    parchment {
        prop("deps.parchment") { mappingsVersion = it }
    }

    var versionName = "${property("mod.version")}-${loaderInitials}-${minecraft}"
    if (!gitBranchName.equals("main")) {
//        versionName += "-$gitBranchName".replace('/', '-')
    }
    // This metadata is used to fill out the information inside
    // the metadata files found in the templates folder.
    metadata {
        modId = "pas"
        modName = "Player Armor Stands"
        modVersion = versionName
        modGroup = "com.danrus.pas"
        modAuthor = "Danrus110_"
        modDescription = "Make named armor stands looks like players!"
        modLicense = "MIT"

        fun MapProperty<String, String>.populate(block: MapProperty<String, String>.() -> Unit) {
            block()
        }

        replacementProperties.populate {
            // You can put any other replacement properties/metadata here that
            // modstitch doesn't initially support. Some examples below.
            put("mod_issue_tracker", "https://discord.com/invite/sBpHZUBebQ")
            put("minecraft_versions", property("version.minecraft") as String)
        }
    }

    // Fabric Loom (Fabric)
    loom {
        fabricLoaderVersion = "0.18.4"

        // Configure loom like normal in this block.
        configureLoom {
            runConfigs.all {
                ideConfigGenerated(environment == "client")
                runDir("../../run")
            }
        }
    }

    // ModDevGradle (NeoForge, Forge, Forgelike)
    moddevgradle {
        prop("deps.forge") { forgeVersion = it }
        prop("deps.neoforge") { neoForgeVersion = it }

        // Configures client and server runs for MDG, it is not done by default
        defaultRuns(server = false)

        // If you want to use the legacy MDG, you can use the following line:

        // This block configures the `neoforge` extension that MDG exposes by default,
        // you can configure MDG like normal from here
        configureNeoForge {
            runs.all {
                gameDirectory = layout.projectDirectory.dir("../../run")
            }
        }
    }

    mixin {
        // You do not need to specify mixins in any mods.json/toml file if this is set to
        // true, it will automatically be generated.
        addMixinsToModManifest = true

        configs.register("pas") {side = CLIENT}
        if (isArmorposer) {
            configs.register("pas.armorposer") {side = CLIENT}
        }

        if (isPossessive) {
            configs.register("pas.possessive") {side = CLIENT}
        }

        if (isEasyAnvils) {
            configs.register("pas.easyanvils") {side = CLIENT}
        }

        if (stonecutter.eval(minecraft, ">=1.21.9")) {
            configs.register("pas.1219")
        }
        // Most of the time you won't ever need loader specific mixins.
        // If you do, simply make the mixin file and add it like so for the respective loader:
        // if (isLoom) configs.register("examplemod-fabric")
        // if (isModDevGradleRegular) configs.register("examplemod-neoforge")
        // if (isModDevGradleLegacy) configs.register("examplemod-forge")
    }
}

// Stonecutter constants for mod loaders.
// See https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants
var constraint: String = name.split("-")[1]
stonecutter {
    constants.apply {
        put("fabric", constraint == "fabric")
        put("neoforge", constraint == "neoforge")
        put("forge", constraint == "forge")
        put("vanilla", constraint == "vanilla")
        put("possessive", isPossessive)
        put("armorposer", isArmorposer)
        put("easyanvils", isEasyAnvils)
        put("modmenu", hasModMenu)
        put("yacl", hasYacl)
    }

    replacements {
        string(current.parsed >= "26.1", "screen_render") {
            replace("render(", "extractRenderState(")
            replace("drawCenteredString(", "centeredText(")
            replace("renderBackground(", "extractBackground(")
            replace("submitEntityRenderState(", "entity(")
        }
        string {
            direction = eval(current.version, ">=26.1")
            replace("net/minecraft/client/renderer/state/CameraRenderState", "net/minecraft/client/renderer/state/level/CameraRenderState")
            replace(".addMessage(", ".addClientSystemMessage(")
            replace("net.minecraft.client.renderer.state.CameraRenderState", "net.minecraft.client.renderer.state.level.CameraRenderState")
            replace("GuiGraphics", "GuiGraphicsExtractor")
        }
        string {
            direction = eval(current.version, ">=1.21.11")
            replace("ResourceLocation", "Identifier")
            replace("import net.minecraft.Util;", "import net.minecraft.util.Util;")
            replace(
                "import net.minecraft.client.model.ArmorStandArmorModel;",
                "import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;"
            )
            replace(
                "import net.minecraft.client.renderer.RenderType;",
                "import net.minecraft.client.renderer.rendertype.RenderTypes;"
            )
            replace(
                "RenderType.",
                "RenderTypes."
            )
        }
        string {
            direction = eval(current.version, "=1.21.10")
            replace("PlayerRenderer", "AvatarRenderer")
        }
    }
}

// All dependencies should be specified through modstitch's proxy configuration.
// Wondering where the "repositories" block is? Go to "stonecutter.gradle.kts"
// If you want to create proxy configurations for more source sets, such as client source sets,
// use the modstitch.createProxyConfigurations(sourceSets["client"]) function.
dependencies {
    modstitch.loom {
        modstitchModImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fapi")}")
        prop("deps.modmenu") {
            modstitchModApi("com.terraformersmc:modmenu:${it}")
        }
        prop("deps.possessive") {
            modstitchModImplementation("maven.modrinth:possessive:${it}")
        }

    }

    // Anything else in the dependencies block will be used for all platforms.
    prop("deps.yacl") {
        modstitchModImplementation("dev.isxander:yet-another-config-lib:${it}")
    }
    prop("deps.armorposer") {
        modstitchModImplementation("com.mrbysco.armorposer:ArmorPoser-${loader}-${property("deps.armorposer")}")
    }

    prop("deps.easyanvils") {
        modstitchModImplementation("maven.modrinth:easy-anvils:${it}")
        modstitchModImplementation("maven.modrinth:puzzles-lib:${property("deps.puzzles")}")
        modstitchModImplementation("maven.modrinth:forge-config-api-port:${property("deps.fcapi")}")
        modstitchModImplementation("com.electronwill.night-config:core:3.8.3")
        modstitchModImplementation("com.electronwill.night-config:toml:3.8.3")
    }
}

publishMods {
    val modrinthToken = findProperty("modrinth-token")
    val curseforgeToken = findProperty("curseforge-token")
    val discordWebhookDR = findProperty("discord-webhook")
    val discordWebhookFrame = findProperty("discord-webhook-frame")
    val discordWebhookDry = findProperty("discord-webhook-dry")

    dryRun = gitBranchName != "main"

    file = modstitch.finalJarTask.flatMap { it.archiveFile }

    changelog = rootProject.file("CHANGELOG.md").readText()
    type = BETA

    val loaders = property("pub.target.platforms").toString().split(' ')
    loaders.forEach(modLoaders::add)
//    modLoaders = loaders
    displayName = "Player Armor Stands ${property("mod.version")} for ${loader} ${minecraft}"
    version = "${property("mod.version")}-${loaderInitials}-${minecraft}"

    val targets = property("pub.target.versions").toString().split(' ')
    val requiresLibs = property("pub.libs.required").toString().split(' ')
    val optionalLibs = property("pub.libs.optional").toString().split(' ')
//    val optionalLibsModrinth = prop("pub.libs.optional.modrinth").toString().split(' ')
    modrinth {
        projectId = property("publish.modrinth").toString()
        accessToken = modrinthToken.toString()
        targets.forEach(minecraftVersions::add)
        requiresLibs.forEach{requires(it)}
        optionalLibs.forEach{optional(it)}
//        optionalLibsModrinth.forEach{optional(it)}
    }

    curseforge {
        projectId = property("publish.curseforge").toString()
        accessToken = curseforgeToken.toString()
        projectSlug = "player-armor-stands"
        targets.forEach(minecraftVersions::add)
        requiresLibs.forEach{requires(it)}
        optionalLibs.forEach{optional(it)}
    }

    if (targets.contains("1.21.4") && loaders.contains("fabric")) {
        discord ("DR freak mods anonuncement") {
            webhookUrl = discordWebhookDR.toString()
            dryRunWebhookUrl = discordWebhookDry.toString()

            username  = "Player Armor Stands"
            avatarUrl = "https://github.com/Danrus1100/PlayerArmorStand/blob/main/src/main/resources/assets/pas/icon.png?raw=true"

            content = changelog.map{ "# " + findProperty("mod.version") + " version here! \n\n" + rootProject.file("CHANGELOG.md").readText() +"\n\n<@&1388295587866083338>"}
        }

        discord ("Frame Server anonuncement") {
            webhookUrl = discordWebhookFrame.toString()
            dryRunWebhookUrl = discordWebhookDry.toString()

            username  = "Player Armor Stands"
            avatarUrl = "https://github.com/Danrus1100/PlayerArmorStand/blob/main/src/main/resources/assets/pas/icon.png?raw=true"

            content = changelog.map{ "# Вышла версия " + findProperty("mod.version") + "! \n\n" + rootProject.file("CHANGELOG_RU.md").readText() + "\n\n<@&1406626250520400092>"}
        }
    }
}