import se.bjurr.gitchangelog.plugin.gradle.GitChangelogTask

plugins {
    kotlin("jvm")
    id("se.bjurr.gitchangelog.git-changelog-gradle-plugin") version "3.1.2"
    id("com.gradleup.shadow") version "9.3.1"
}
/**
 * NOTE: This is entirely optional and basics can be done in `settings.gradle.kts`
 */

repositories {
    // Any external repositories besides: MavenLocal, MavenCentral, HytaleMaven, and CurseMaven
    mavenCentral()
}

dependencies {
    // Any external dependency you also want to include
    implementation(kotlin("stdlib-jdk8"))

    // Unit Testing
    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk:1.14.9")
}
kotlin {
    jvmToolchain(25)
}

tasks.shadowJar {
    // Removed the `-all` suffix from the Shadow JAR
    archiveClassifier.set("")
}

tasks.named<GitChangelogTask>("gitChangelog") {
    ignoreTagsIfNameMatches.set(".*-(alpha|beta|rc).*")
    templateContent.set(getChangelogTemplate(true))
}


tasks.register<GitChangelogTask>("gitChangelogConsumer") {
    file.set(file("CHANGELOG_CONSUMER.md"))
    ignoreTagsIfNameMatches.set(".*-(alpha|beta|rc).*")
    templateContent.set(getChangelogTemplate(false))
}

tasks.register<GitChangelogTask>("gitChangelogRelease") {
    val preReleaseSuffixes = listOf("-alpha", "-beta", "-rc")

    fun getTag(startingPoint: String, excludes: List<String> = listOf()): Provider<String> = providers.exec {
        commandLine(
            "git", "describe", "--tags", "--abbrev=0", "--match", "v*",
            startingPoint,
            *excludes.map { "--exclude=*$it*" }.toTypedArray()
        )
    }.standardOutput.asText.map { it.trim() }


    val currentTag = getTag("HEAD")

    val isPreRelease = preReleaseSuffixes.any { currentTag.get().contains(it) }

    var prevTag: Provider<String>? = null
    if (isPreRelease) {
        prevTag = getTag("${currentTag.get()}^")
    } else {
        prevTag = getTag("${currentTag.get()}^", preReleaseSuffixes)
        ignoreTagsIfNameMatches.set(".*-(alpha|beta|rc).*")
    }

    file.set(file("CHANGELOG_RELEASE.md"))
    fromRevision.set(prevTag)
    toRevision.set(currentTag)

    templateContent.set(getChangelogTemplate(true))
}

fun getChangelogTemplate(technical: Boolean): String = buildString {
    appendLine("{{#tags}}")

    if (technical) {
        appendLine("## [{{name}}](https://github.com/LordTkay/HyFlask/releases/tag/{{name}}) ({{tagDate .}})")
    } else {
        appendLine("## {{name}} ({{tagDate .}})")
    }

    appendLine()
    append(getChangelogSection("feature", "Features", technical))
    append(getChangelogSection("fix", "Bug Fixes", technical))
    append(getChangelogSection("gamedep", "Hytale Dependency Changes", technical))
    if (technical) {
        append(getChangelogSection("refactor", "Refactoring", true))
        append(getChangelogSection("docs", "Documentation", true))
        append(getChangelogSection("chore", "Chores", true))
    }
    appendLine("{{/tags}}")
}


fun getChangelogSection(tag: String, title: String, technical: Boolean): String = buildString {
    appendLine("{{#ifContainsType commits type='$tag'}}")
    appendLine("### $title")
    appendLine()
    appendLine("{{#commits}}")

    appendLine("{{#ifCommitType . type='$tag'}}")
    if (technical) {
        appendLine("- {{#eachCommitScope .}} **{{.}}** {{/eachCommitScope}} {{{commitDescription .}}} ([{{hash}}](https://github.com/LordTkay/HyFlask/commit/{{hashFull}}))")
    } else {
        appendLine("- {{#eachCommitScope .}} **{{.}}** {{/eachCommitScope}} {{{commitDescription .}}}")
    }
    appendLine("{{/ifCommitType}}")
    appendLine("{{/commits}}")
    appendLine()
    appendLine("{{/ifContainsType}}")
}


