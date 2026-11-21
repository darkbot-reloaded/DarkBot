plugins {
    `java-library`
    id("org.gradle.maven-publish")
    id("org.gradle.application")

    id("io.freefair.lombok") version "9.8.3"
}

buildscript {
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.10.0")
    }
}

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://jitpack.io") {
        content {
            includeGroup("eu.darkbot.DarkBotAPI")
        }
    }
}

group = "eu.darkbot"
version = "1.131"
description = "DarkBot"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

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

val proguardLibraries = configurations.create("proguardLibraries") {
    isCanBeConsumed = false
    isCanBeResolved = true
    extendsFrom(configurations.compileOnly.get())
}

dependencies {
    val apiVersion = "0.9.12"
    val flatLafVersion = "3.4"

    // use this if you want to use local(mavenLocal) darkbot API
    val useLocalMaven = false
    if (useLocalMaven) api("eu.darkbot:darkbot-impl:$apiVersion")
    else api("eu.darkbot.DarkBotAPI:darkbot-impl:$apiVersion")

    // have to keep version 2.8.9, in newer versions GSON calls `toString` of config enums upon creation
    api("com.google.code.gson:gson:2.8.9")
    api("com.miglayout:miglayout-swing:11.3")
    api("com.formdev:flatlaf:$flatLafVersion")
    api("com.formdev:flatlaf-extras:$flatLafVersion")
    api("org.jgrapht:jgrapht-core:1.5.3")
    api("it.unimi.dsi:fastutil-core:8.5.19")

    // Testing stat time-series requires this
    //api("org.knowm.xchart", "xchart", "3.8.5")

    compileOnly("org.jetbrains:annotations:26.1.0")

    testImplementation(platform("org.junit:junit-bom:5.14.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    //testImplementation("org.mockito:mockito-core:4.11.0")
}

tasks.test { useJUnitPlatform() }
tasks.withType<JavaCompile> { options.encoding = "UTF-8" }
tasks.withType<JavaExec> { systemProperty("file.encoding", "UTF-8") }

tasks.wrapper {
    gradleVersion = "9.9.2"

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
    val toExclude = createExcludes(
        "it.unimi.dsi.fastutil.bytes.**",
        "it.unimi.dsi.fastutil.chars.**",
        "it.unimi.dsi.fastutil.doubles.**",
        "it.unimi.dsi.fastutil.floats.**",
        "it.unimi.dsi.fastutil.shorts.**",
        "it.unimi.dsi.fastutil.booleans.**"
    )

    dontoptimize()
    dontobfuscate()

    dontwarn("java.lang.invoke.**")
    dontwarn("com.google.errorprone.annotations.**")

    keepattributes("Signature")
    keep("class com.github.manolo8.** { *; }")
    keep("class eu.darkbot.** { *; }")
    keep("class com.formdev.** { *; }")
    keep("class com.github.weisj.jsvg.** { *; }")

    injars(tasks.jar.get())
    outjars(toExclude, "build/DarkBot.jar")

    libraryjars(proguardLibraries.files)
    libraryjars(
        mapOf(
            "jarfilter" to "!**.jar",
            "filter" to "!module-info.class",
        ), javaToolchains.launcherFor(java.toolchain).get().metadata.installationPath.dir("jmods").asFile
    )

    dependsOn(tasks.build)
}

fun createExcludes(vararg excludes: String): Map<String, String> {
    return mapOf("filter" to excludes.joinToString(separator = ",", transform = { "!${it.replace(".", "/")}" }))
}
