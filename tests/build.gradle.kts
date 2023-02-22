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

tasks.register("cloneAlibaba") {
    doFirst {
        val repo = Git.cloneRepository()
            .setURI("https://github.com/alibaba/spring-cloud-alibaba.git")
            .setDirectory(File("${project.projectDir}/testrepo"))
            .call()
        repo.checkout()
            .setName("583d287687afef3ed15a378b932854a1b5570fa7") // last 2022 commit
            .call()
    }
}