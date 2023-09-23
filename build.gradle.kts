plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

allprojects {
    apply(plugin = "java")

    group = "me.mattstudios"
    version = "1.0"

    repositories {
        mavenCentral()
        mavenLocal()

        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://jitpack.io")
        maven("https://repo.dmulloy2.net/nexus/repository/public/")
    }

    dependencies {

        // Paper
        compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
        // ProtocolLib
        compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")

        // JetBrains annotations
        compileOnly("org.jetbrains:annotations:22.0.0")

        // My stuff
        implementation("me.mattstudios.utils:matt-framework:1.4.6")

        implementation("commons-validator:commons-validator:1.7")

        implementation("com.github.sealedtx:java-youtube-downloader:3.2.3")

        implementation("org.jcodec:jcodec:0.2.5")
        implementation("org.jcodec:jcodec-android:0.2.5")
        implementation("org.jcodec:jcodec-javase:0.2.5")
        implementation("net.coobird:thumbnailator:0.4.17")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
        options.isFork = true
        options.encoding = "UTF-8"
    }
}

tasks {
    shadowJar {

        relocate("me.mattstudios.mf", "me.mattstudios.triumphpets.mf")
        archiveFileName = "Holovid.jar"
    }

    processResources {
        expand("version" to rootProject.version)
    }
}
