plugins {
    id("org.gradle.java-library")
    id("org.gradle.maven-publish")
    id("org.gradle.application")

    id("io.freefair.lombok") version "8.6"
}

buildscript {
    dependencies {
        classpath("com.guardsquare", "proguard-gradle", "7.4.2")
    }
}

repositories {
    mavenLocal()
    mavenCentral()

    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://oss.jfrog.org/artifactory/oss-snapshot-local/com/formdev/") }
}

group = "eu.darkbot"
version = "1.130"
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

configurations {
    compileOnly {
        isCanBeResolved = true
    }
}

dependencies {
    val apiVersion = "0.9.4"
    val flatLafVersion = "3.4"

    // use this if you want to use local(mavenLocal) darkbot API
    //api("eu.darkbot", "darkbot-impl", apiVersion)
    api("eu.darkbot.DarkBotAPI", "darkbot-impl", apiVersion)

    api("com.google.code.gson", "gson", "2.8.9")
    api("com.miglayout", "miglayout-swing", "11.3")
    api("com.formdev", "flatlaf", flatLafVersion)
    api("com.formdev", "flatlaf-extras", flatLafVersion)
    api("org.jgrapht", "jgrapht-core", "1.3.0")
    api("it.unimi.dsi", "fastutil-core", "8.5.13")

    // Testing stat time-series requires this
    //api("org.knowm.xchart", "xchart", "3.8.5")

    compileOnly("org.jetbrains", "annotations", "24.1.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("org.mockito:mockito-core:4.10.0")
}

tasks.withType<JavaCompile> { options.encoding = "UTF-8" }
tasks.withType<JavaExec> { systemProperty("file.encoding", "UTF-8") }

tasks.wrapper {
    gradleVersion = "8.6"

    // with gradle javadocs and sources
    distributionType = Wrapper.DistributionType.ALL
}

tasks.jar {
    manifest {
        attributes["SplashScreen-Image"] = "icon.png"
        attributes["Main-Class"] = application.mainClass
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map(::zipTree))
}

tasks.register<proguard.gradle.ProGuardTask>("proguard") {
    dontoptimize()
    dontobfuscate()
    dontnote()
    dontwarn()

    keepattributes("Signature")
    keep("class com.github.manolo8.** { *; }")
    keep("class eu.darkbot.** { *; }")
    keep("class com.formdev.** { *; }")
    keep("class com.github.weisj.jsvg.** { *; }")

    injars(tasks.jar.get())
    outjars("build/DarkBot.jar")

    libraryjars(configurations.compileOnly.get().files)
    libraryjars(
        mapOf(
            "jarfilter" to "!**.jar",
            "filter" to "!module-info.class",
        ), "${System.getProperty("java.home")}/jmods"
    )

    dependsOn(tasks.build)
}
