plugins {
    kotlin("multiplatform") version Kotlin.version apply false
}

allprojects {
    repositories {
        jcenter()
        mavenCentral()
        mavenLocal()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://kotlin.bintray.com/kotlinx")
        maven("https://repo.opencollab.dev/maven-snapshots/")
        maven("https://repo.opencollab.dev/maven-releases/")
    }
}