plugins {
    java
    kotlin("jvm")
}

group = "io.github.shinglem"
repositories {
    mavenCentral()
}

dependencies {
    api(project(":vertx-web-extension"))
    api(project(":default-class-util"))

    api("ch.qos.logback", "logback-classic", "1.2.3")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

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