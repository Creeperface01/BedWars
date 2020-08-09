import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import proguard.gradle.ProGuardTask

val jacksonVersion = "2.10.1"

plugins {
//    kotlin("jvm") version "1.3.70"
    kotlin("jvm") version "1.4.0-rc"
}

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("net.sf.proguard:proguard-gradle:6.2.2")
    }
}

repositories {
    jcenter()
    mavenCentral()
    mavenLocal()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://kotlin.bintray.com/kotlinx")
    maven("http://repo.nukkitx.com/main")
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly(kotlin("reflect"))
    compileOnly("cn.nukkit:nukkit:1.0-SNAPSHOT")
    compileOnly("ru.nukkit.dblib:DbLib:1.0-SNAPSHOT")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")

    implementation("commons-io:commons-io:2.6")
    implementation("org.apache.httpcomponents:httpclient:4.5.12")
    implementation("org.apache.commons:commons-lang3:3.9")
    implementation("org.jooq:joor-java-8:0.9.7")
    implementation("org.mongodb:mongo-java-driver:3.12.2")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("org.jooq:joor-java-8:0.9.12")
    implementation("com.creeperface.nukkit.kformapi:KFormAPI:1.0-SNAPSHOT")


    compileOnly(files("lib/actaeon.jar", "lib/ScoreboardAPI.jar", "lib/EconomyAPI.jar"))
}

kotlin {
    experimental.coroutines = Coroutines.ENABLE
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.apply {
    jvmTarget = "1.8"
    freeCompilerArgs = listOf("-Xjvm-default=enable", "-Xopt-in=kotlin.RequiresOptIn")
}

tasks {
    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveClassifier.set("core")

        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        exclude(
                "com/creeperface/nukkit/placeholderapi/**",
                "kotlin/**",
                "org/yaml/snakeyaml/**"
        )
    }

    val apiJar by creating(Jar::class) {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveClassifier.set("api")

        from(sourceSets["main"].output) {
            include("com/creeperface/nukkit/bedwars/api/**")
            exclude(
                    "org/**"
            )
        }
    }

    val libJar by creating(Jar::class) {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveClassifier.set("libs")

        from(configurations.compileOnly.get().map { if (it.isDirectory) it else zipTree(it) })
    }

    val proguardTask by creating(ProGuardTask::class) {
        dependsOn(jar)
        dependsOn(libJar)

        configuration("proguard.txt")

        injars("$buildDir/libs/BedWars-core.jar")
        outjars("$buildDir/libs/BedWars.jar")
    }

    artifacts {
        archives(apiJar)
        archives(libJar)
//        archives(proguardTask.outputs.files.singleFile) {
//            builtBy(proguardTask)
//        }
    }
}