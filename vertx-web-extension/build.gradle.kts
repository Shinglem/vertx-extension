plugins {
    java
    kotlin("jvm")
}

group = "io.github.shinglem"
repositories {
    mavenCentral()
}

val vertxVersion: String by project
dependencies {
    api(project(":vertx-core-extension"))
    api(project(":default-class-util"))

    api(group = "io.vertx", name = "vertx-web", version = vertxVersion)

    api(kotlin("reflect"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("ch.qos.logback", "logback-classic", "1.2.3")
    testImplementation("io.vertx", "vertx-web-client", vertxVersion)
    testImplementation("junit", "junit", "4.12")
}

tasks {

    compileJava {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    }
    compileTestJava {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    }
    compileKotlin {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

}