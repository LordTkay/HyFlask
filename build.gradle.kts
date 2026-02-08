plugins {
    kotlin("jvm")
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
