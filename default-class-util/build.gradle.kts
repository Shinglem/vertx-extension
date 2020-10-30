plugins {
    java
    kotlin("jvm")
}

group = "io.github.shinglem"
repositories {
    mavenCentral()
}

dependencies {
    api(kotlin("reflect"))
    api(project(":common"))
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