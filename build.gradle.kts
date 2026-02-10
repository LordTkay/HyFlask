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
}
kotlin {
    jvmToolchain(25)
}

tasks.shadowJar {
    // Removed the `-all` suffix from the Shadow JAR
    archiveClassifier.set("")

    relocate("kotlin", "de.lordtkay.hyflask.shadow.kotlin")
}

tasks.named<GitChangelogTask>("gitChangelog") {
    templateContent.set(getChangelogTemplate(true))
}


tasks.register<GitChangelogTask>("gitChangelogConsumer") {
    file.set(file("CHANGELOG_CONSUMER.md"))
    templateContent.set(getChangelogTemplate(false))
}

fun getChangelogTemplate(technical: Boolean): String = buildString {
    appendLine("{{#tags}}")
    appendLine("{{#ifReleaseTag .}}")

    if (technical) {
        appendLine("## [{{name}}](https://gitlab.com/html-validate/html-validate/compare/{{name}}) ({{tagDate .}})")
    } else {
        appendLine("## {{name}} ({{tagDate .}})")
    }

    appendLine()
    append(getChangelogSection("feat", "Features", technical))
    append(getChangelogSection("fix", "Bug Fixes", technical))
    append(getChangelogSection("gamedep", "Hytale Dependency Changes", technical))
    if (technical) {
        append(getChangelogSection("refactor", "Refactoring", true))
        append(getChangelogSection("docs", "Documentation", true))
        append(getChangelogSection("chore", "Chores", true))
    }
    appendLine("{{/ifReleaseTag}}")
    appendLine("{{/tags}}")
}


fun getChangelogSection(tag: String, title: String, technical: Boolean): String = buildString {
    appendLine("{{#ifContainsType commits type='$tag'}}")
    appendLine("### $title")
    appendLine()
    appendLine("{{#commits}}")

    appendLine("{{#ifCommitType . type='$tag'}}")
    if (technical) {
        appendLine("- {{#eachCommitScope .}} **{{.}}** {{/eachCommitScope}} {{{commitDescription .}}} ([{{hash}}](https://gitlab.com/html-validate/html-validate/commit/{{hashFull}}))")
    } else {
        appendLine("- {{#eachCommitScope .}} **{{.}}** {{/eachCommitScope}} {{{commitDescription .}}}")
    }
    appendLine("{{/ifCommitType}}")
    appendLine("{{/commits}}")
    appendLine()
    appendLine("{{/ifContainsType}}")
}


