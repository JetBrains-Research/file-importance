import org.jetbrains.intellij.ideaDir
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.7.0"
    kotlin("jvm") version "1.5.10"
    application
}

group = "me.psycho"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

intellij {
    version.set("2022.1.1")
    type.set("IC")
    plugins.set(listOfNotNull("java", "Kotlin"))
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("MainKt")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }

    runIde {
//        ideDir.set(file("/Users/psycho/Desktop/Jetbrains/simple-springboot-app"))
        args = listOfNotNull("mine-dependencies")
        jvmArgs = listOf("-Djava.awt.headless=true")
    }

    register("extractDependencies") {
        dependsOn(runIde)
    }
}