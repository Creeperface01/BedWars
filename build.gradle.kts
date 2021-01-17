import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import proguard.gradle.ProGuardTask

val jacksonVersion = "2.10.1"
val kotlinVersion = "1.4.21"
val kotlinCoroutinesVersion = "1.4.2"

plugins {
    kotlin("jvm") version "1.4.21"

    id("org.zeroturnaround.gradle.jrebel") version "1.1.10"
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
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.opencollab.dev/maven-releases/")
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8", kotlinVersion))
    compileOnly(kotlin("reflect", kotlinVersion))
    compileOnly("cn.nukkit:nukkit:1.0-SNAPSHOT")
    compileOnly("ru.nukkit.dblib:DbLib:1.0-SNAPSHOT")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    compileOnly("com.creeperface.nukkit.kformapi:KFormAPI:1.0-SNAPSHOT")

    implementation("commons-io:commons-io:2.6")
    implementation("org.apache.httpcomponents:httpclient:4.5.12")
    implementation("org.apache.commons:commons-lang3:3.9")
    implementation("org.mongodb:mongo-java-driver:3.12.2")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
    implementation("org.jooq:joor-java-8:0.9.13")


    compileOnly(files("lib/actaeon.jar", "lib/ScoreboardAPI.jar", "lib/EconomyAPI.jar"))
}

//kotlin {
//    experimental.coroutines = Coroutines.ENABLE
//}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.apply {
    jvmTarget = "1.8"
    freeCompilerArgs = listOf("-Xjvm-default=enable", "-Xopt-in=kotlin.RequiresOptIn")
}

tasks {
    val aliasFix by creating {
        outputs.upToDateWhen { false }

        doLast {
            val out = withType<KotlinCompile>().map { it.destinationDir }.first()

            val papiDir = File(out, "com/creeperface/nukkit/placeholderapi")
            papiDir.deleteRecursively()
        }
    }
    aliasFix.dependsOn(build.get())
    build.get().finalizedBy(aliasFix)

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