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

fun log(message: String) {
    println(message)
}

tasks.test {

    val repoPath = project.property("repo.path") as String
    val repoName = project.property("repo.name") as String

    log("Launch calculation")

    exec {
        standardOutput = System.out
        errorOutput = System.err
        isIgnoreExitValue = true
        workingDir(File("../").path)
        commandLine("./test-run.sh", repoPath)
    }

    val actualPath = project.property("outputs.actual") as String
    val expectedPath = project.property("outputs.expected") as String

    delete(expectedPath)
    delete(actualPath)

    mkdir("outputs")

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