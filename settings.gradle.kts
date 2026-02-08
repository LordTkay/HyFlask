import dev.scaffoldit.hytale.wire.HytaleManifest

rootProject.name = "hyflask"

pluginManagement {
    plugins {
        kotlin("jvm") version "2.3.10"
    }
}

plugins {
    // See documentation on https://scaffoldit.dev
    id("dev.scaffoldit") version "0.2.+"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Would you like to do a split project?
// Create a folder named "common", then configure details with `common { }`

hytale {
    usePatchline("release")
    useVersion("latest")

    repositories {
        // Any external repositories besides: MavenLocal, MavenCentral, HytaleMaven, and CurseMaven
    }

    dependencies {
        // Any external dependency you also want to include
    }

    manifest {
        Group = "LordTkay"
        Name = "HyFlask"
        Version = "1.0.0"
        Description = "A mod that adds reusable and upgradable flasks!"
        Main = "de.lordtkay.hyflask.HyFlaskPlugin"
        Website = "https://www.curseforge.com/hytale/mods/hyflask"
        Authors = listOf(
            HytaleManifest.Author("Lord Tkay", "tobawa2601@gmail.com", "https://github.com/LordTkay")
        )
        IncludesAssetPack = true
        Dependencies = mapOf(
            "Hytale:Beds" to "*"
        )
    }
}
