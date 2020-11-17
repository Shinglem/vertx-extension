
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.transport.URIish


plugins {
    java
    kotlin("jvm")
    `maven-publish`
}
group = "com.github.Shinglem.vertx-extension"

buildscript {
    repositories {
        mavenLocal()
        maven("http://maven.aliyun.com/nexus/content/groups/public/")
        maven("https://dl.bintray.com/kotlin/exposed")
        maven("https://jitpack.io")
        jcenter()
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
        maven("https://clojars.org/repo/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        mavenCentral()
        maven { setUrl("https://plugins.gradle.org/m2/") }
    }
    dependencies {
        "classpath"(group = "org.eclipse.jgit", name = "org.eclipse.jgit", version = "3.5.0.201409260305-r")
    }
}


//group = "io.github.shinglem"
repositories {
    mavenCentral()
}

val vertxVersion: String by project
dependencies {
    api(project(":vertx-core-extension"))
    api(project(":default-class-util"))

    api(group = "io.vertx", name = "vertx-web", version = vertxVersion)

    api(kotlin("reflect"))
    implementation(group = "org.eclipse.jgit", name = "org.eclipse.jgit", version = "3.5.0.201409260305-r")
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



publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            // change to point to your repo, e.g. http://my.org/repo
            url = uri("https://maven.pkg.github.com/Shinglem/maven-repository")

            credentials {
                username = "Shinglem"
                password = "0a81b190e44c18acaaab367269e38521793131cc"
            }

        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.shinglem"
            artifactId = "vertx-web-extension"
            version = "0.1"

            from(components["java"])


            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }

        }
    }
}



tasks {

    create<Copy>("copy-maven"){
        dependsOn("publish")
        from("$buildDir/repo")
        into("$buildDir/github")
    }

    create("upload") {
        dependsOn("copy-maven")
        doLast {

            org.eclipse.jgit.api.Git.init()
                .setDirectory(File("$buildDir/github"))
                .setBare(false)
                .call()

            val repo = org.eclipse.jgit.storage.file.FileRepositoryBuilder()
                .setWorkTree(File("$buildDir/github"))
                .build()

            val config = repo.config
            val remoteConfig = org.eclipse.jgit.transport.RemoteConfig(config, "origin")
            val uri: URIish = URIish("https://github.com/Shinglem/maven-repository.git")
            remoteConfig.addURI(uri)
            remoteConfig.addFetchRefSpec(org.eclipse.jgit.transport.RefSpec("+refs/heads/*:refs/remotes/origin/*"))
            remoteConfig.update(config)
            config.save()

            val git = org.eclipse.jgit.api.Git.open(File("$buildDir/github"))

            val branch = "refs/heads/master"

            val spec = RefSpec("$branch:refs/remotes/origin/master")

            git.add()
                .addFilepattern(".")
                .call()

            git.commit()
                .setMessage("t")
                .call()


            git.push()
                .setRemote("origin")
                .setCredentialsProvider(
                    org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider(
                        "Shinglem", "agdb407645310"
                    )
                )
                .setRefSpecs(spec)
                .call()
        }
    }
}