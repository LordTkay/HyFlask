import dev.scaffoldit.hytale.wire.HytaleManifest

rootProject.name = "HyFlask"

pluginManagement {
    plugins {
        kotlin("jvm") version "2.3.10"
    }
}

plugins {
    // See documentation on https://scaffoldit.dev
    id("dev.scaffoldit") version "0.2.13"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Would you like to do a split project?
// Create a folder named "common", then configure details with `common { }`

hytale {
    usePatchline("release")
    useVersion("latest")
    useKotlin()

    repositories {
        // Any external repositories besides: MavenLocal, MavenCentral, HytaleMaven, and CurseMaven
    }

    dependencies {
        // Any external dependency you also want to include
    }

    manifest {
        Group = "LordTkay"
        Name = "HyFlask"
        Version = extra["version"].toString()
        Description = "A mod that adds reusable and upgradable flasks!"
        Main = "de.lordtkay.hyflask.HyFlaskPlugin"
        Website = "https://www.curseforge.com/hytale/mods/hyflask"
        Authors = listOf(
            HytaleManifest.Author("Lord Tkay", "tobawa2601@gmail.com", "https://github.com/LordTkay")
        )
        IncludesAssetPack = true
        ServerVersion = "2026.03.26-89796e57b"
        Dependencies = mapOf(
            "Hytale:Beds" to "*",
            "com.hypersonicsharkz:Hytalor" to "2.2"
        )
    }
}
