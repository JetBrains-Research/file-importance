import org.eclipse.jgit.api.Git

plugins {
    id("java")
    kotlin("jvm") version "1.8.0"
}

group = "com.jetbrains.research.ictl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
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