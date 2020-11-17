/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 *
 * Detailed information about configuring a multi-project build in Gradle can be found
 * in the user manual at https://docs.gradle.org/6.5.1/userguide/multi_project_builds.html
 */
pluginManagement {
    repositories {

        mavenLocal()
        maven("http://maven.aliyun.com/nexus/content/groups/public/")
        maven("https://dl.bintray.com/kotlin/exposed")
        maven("https://jitpack.io")
        jcenter()
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
        maven("https://clojars.org/repo/")
        maven { setUrl("https://plugins.gradle.org/m2/") }
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://maven.pkg.github.com/Shinglem/maven-repository")
        mavenCentral()
    }

    plugins {
        val kotlinVersion:String by settings
        val springBootVersion: String by settings
        id( "org.jetbrains.kotlin.jvm")  version kotlinVersion
        id ("org.jetbrains.kotlin.plugin.noarg")  version kotlinVersion
        id ("org.jetbrains.kotlin.plugin.allopen")  version kotlinVersion
        kotlin("kapt") version kotlinVersion
        id("com.github.johnrengelman.shadow") version "5.1.0"
    }


}
rootProject.name = "vertx-extension"
include("common")
include("vertx-core-extension")
include("vertx-web-extension")
include("default-class-util")
include("example")
