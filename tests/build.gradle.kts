import org.eclipse.jgit.api.Git
import java.io.ByteArrayOutputStream

plugins {
    id("java")
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.10"
}

group = "com.jetbrains.research.ictl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:2.36.1")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
}



tasks.register("cloneTestRepo") {
    val folderName = project.property("testrepo.folder") as String
    val repoUrl = project.property("testrepo.url") as String

    val repoFile = File("${project.projectDir}/$folderName")

    doFirst {
        if (repoFile.exists()) {
            println("repo seems to have been cloned already")
            return@doFirst
        }
        val repo = Git.cloneRepository()
            .setURI(repoUrl)
            .setDirectory(repoFile)
            .call()
        if (project.hasProperty("testrepo.commit")) {
            val commit = project.property("testrepo.commit") as String
            repo.checkout()
                .setName(commit)
                .call()
        }
    }
}

fun log(message: String) {
    println(message)
}

tasks.test {

    val repoPath = project.property("repo.path") as String
    val repoOwner = project.property("repo.owner") as String
    val repoName = project.property("repo.name") as String

    log("Launch calculation")

    exec {
        standardOutput = System.out
        errorOutput = System.err
        isIgnoreExitValue = true
        workingDir(File("../").path)
        commandLine("./test-run.sh", repoPath, repoOwner, repoName)
    }

    val actualPath = project.property("outputs.actual") as String
    val expectedPath = project.property("outputs.expected") as String

    delete(expectedPath)
    delete(actualPath)

    copy {
        from("../output")
        into(actualPath)
    }

    copy{
        from("specs/$repoName")
        into(expectedPath)
    }

    useJUnitPlatform()
}