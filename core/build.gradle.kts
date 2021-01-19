import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")

    id("org.zeroturnaround.gradle.jrebel") version "1.1.10"
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.apply {
    jvmTarget = "1.8"
    freeCompilerArgs = listOf("-Xjvm-default=enable", "-Xopt-in=kotlin.RequiresOptIn")
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8", Kotlin.version))
    compileOnly(kotlin("reflect", Kotlin.version))
    compileOnly("cn.nukkit:nukkit:1.0-SNAPSHOT")
    compileOnly("ru.nukkit.dblib:DbLib:1.0-SNAPSHOT")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Kotlin.coroutinesVersion}")
    compileOnly("com.creeperface.nukkit.kformapi:KFormAPI:1.0-SNAPSHOT")
    compileOnly("com.creeperface.nukkit.placeholderapi:PlaceholderAPI:${PlaceholderAPI.version}")

    implementation(project(":api"))
    implementation("commons-io:commons-io:2.6")
    implementation("org.apache.httpcomponents:httpclient:4.5.12")
    implementation("org.apache.commons:commons-lang3:3.9")
    implementation("org.mongodb:mongo-java-driver:3.12.2")
    implementation("com.fasterxml.jackson.core:jackson-core:${Jackson.version}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${Jackson.version}")
    implementation("com.fasterxml.jackson.core:jackson-annotations:${Jackson.version}")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${Jackson.version}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:${Jackson.version}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Jackson.version}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Jackson.version}")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:${Jackson.version}")
    implementation("org.jooq:joor-java-8:0.9.13")


    compileOnly(files("lib/actaeon.jar", "lib/ScoreboardAPI.jar", "lib/EconomyAPI.jar"))
}

tasks {

    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveBaseName.set("BedWars")

        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        exclude(
            "kotlin/**",
            "org/yaml/snakeyaml/**"
        )
    }

//    val libJar by creating(Jar::class) {
//        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//        archiveClassifier.set("libs")
//
//        from(configurations.compileOnly.get().map { if (it.isDirectory) it else zipTree(it) })
//    }
//
//    artifacts {
//        archives(libJar)
//    }
}