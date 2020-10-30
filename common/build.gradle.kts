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

    //vertx
    api(group = "io.vertx", name = "vertx-core", version = vertxVersion)
    api(group = "io.vertx", name = "vertx-lang-kotlin", version = vertxVersion)
    api(group = "io.vertx", name = "vertx-lang-kotlin-coroutines", version = vertxVersion)


    //jackson
    api("com.fasterxml.jackson.core:jackson-databind:2.10.2")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.2") {
        exclude("org.jetbrains.kotlin" , "kotlin-reflect")
    }
    api("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.10.2")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.10.2")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.10.2")

    //log
    api("org.slf4j:slf4j-api:1.7.25")

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