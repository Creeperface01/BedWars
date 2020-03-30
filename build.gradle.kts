import org.jetbrains.kotlin.gradle.dsl.Coroutines

val jacksonVersion = "2.10.1"

plugins {
    kotlin("jvm") version "1.3.70"
}

repositories {
    jcenter()
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://kotlin.bintray.com/kotlinx")
    maven("http://repo.nukkitx.com/main")
}

dependencies {
    api(kotlin("stdlib-jdk8"))
    api(kotlin("reflect"))
    api("cn.nukkit:nukkit:1.0-SNAPSHOT")
    api("ru.nukkit.dblib:DbLib:1.0-SNAPSHOT")
    api("gt.creeperface.nukkit.scoreboardapi:ScoreboardAPI:1.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")
    implementation("commons-io:commons-io:2.6")
    implementation("org.apache.commons:commons-lang3:3.9")
    implementation("org.jooq:joor-java-8:0.9.7")
    implementation("rg.mongodb:mongodb-driver-sync:3.11.2")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
}

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}