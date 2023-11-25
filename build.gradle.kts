plugins {
    id("org.gradle.java-library")
    id("org.gradle.maven-publish")
    id("org.gradle.application")

    id("io.freefair.lombok") version "6.6.1"
    id("org.beryx.runtime") version "1.12.7"
    id("edu.sc.seis.launch4j") version "2.5.3"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

buildscript {
    dependencies {
        classpath("com.guardsquare", "proguard-gradle", "7.2.2")
    }
}

repositories {
    mavenLocal()
    mavenCentral()

    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://oss.jfrog.org/artifactory/oss-snapshot-local/com/formdev/") }
}

group = "eu.darkbot"
version = "1.128"
description = "DarkBot"
java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11

application {
    applicationName = "DarkBot"
    mainClass.set("com.github.manolo8.darkbot.Bot")
}

publishing {
    java.withSourcesJar()

    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

dependencies {
    val apiVersion = "0.8.3"
    val flatLafVersion = "3.1.1"

    // use this if you want to use local(mavenLocal) darkbot API
//    api("eu.darkbot", "darkbot-impl", apiVersion)
    api("eu.darkbot.DarkBotAPI", "darkbot-impl", apiVersion)

    api("com.google.code.gson", "gson", "2.8.9")
    api("com.miglayout", "miglayout", "3.7.4")
    api("com.formdev", "flatlaf", flatLafVersion)
    api("com.formdev", "flatlaf-extras", flatLafVersion)
    api("org.jgrapht", "jgrapht-core", "1.3.0")

    // Testing stat time-series requires this
    //api("org.knowm.xchart", "xchart", "3.8.5")

    compileOnly("org.jetbrains", "annotations", "23.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("org.mockito:mockito-core:4.10.0")
}

tasks.withType<JavaCompile> { options.encoding = "UTF-8" }
tasks.withType<JavaExec> { systemProperty("file.encoding", "UTF-8") }

tasks.wrapper {
    gradleVersion = "7.5.1"

    // without gradle javadocs and sources
    distributionType = Wrapper.DistributionType.BIN
}

tasks.jar {
    manifest {
        attributes["SplashScreen-Image"] = "icon.png"
    }
}

tasks.register<proguard.gradle.ProGuardTask>("proguard") {
    allowaccessmodification()
    dontoptimize()
    dontobfuscate()
    dontnote()
    dontwarn()

    keepattributes("Signature")
    keep("class com.github.manolo8.** { *; }")
    keep("class eu.darkbot.** { *; }")
    keep("class com.formdev.** { *; }")

    injars(tasks["shadowJar"].outputs.files.singleFile)
    outjars("build/DarkBot.jar")

    if (JavaVersion.current().isJava9Compatible) {
        libraryjars("${System.getProperty("java.home")}/jmods")
    } else {
        libraryjars("${System.getProperty("java.home")}/lib/rt.jar")
    }

    dependsOn(tasks.build)
}

launch4j {
    productName = "DarkBot"
    jarTask = project.tasks["proguard"]
    icon = "$projectDir/icon.ico"

    maxHeapSize = 512
    version = project.version.toString()

    jreRuntimeBits = "64/32" // will prioritize 64bit jre/jdk

    copyConfigurable = listOf<Any>()
    supportUrl = "https://darkbot.eu"
}

//will execute proguard task after build
//tasks.build {
//    finalizedBy(":proguard")
//}

// need to download WiX tools!
runtime {
    options.addAll("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
    modules.addAll(
        "java.desktop",
        "java.scripting",
        "java.logging",
        "java.xml",
        "java.datatransfer",
        "jdk.crypto.cryptoki"
    )
    jpackage {
        if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
            installerType = "msi"
            installerOptions.addAll(
                listOf(
                    "--win-per-user-install",
                    "--win-shortcut",
                    "--win-menu"
                )
            )
        } else installerOptions.addAll(listOf("--icon", "icon.ico")) //not possible with .msi type

        imageOptions.addAll(listOf("--icon", "icon.ico"))
    }
}
