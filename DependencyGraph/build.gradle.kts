import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.intellij.tasks.RunIdeTask

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.13.0"
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
    application
}

group = "me.psycho"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://www.jetbrains.com/intellij-repository/releases/")
}

intellij {
    version.set("2022.3.2")
    type.set("IC")
    plugins.set(listOfNotNull("java", "Kotlin"))
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
//    implementation("com.jetbrains.intellij.platform:warmup:221.5921.22")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass.set("MainKt")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    compileJava{
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    runIde {
        val deplevel: String? by project
        val projectpath: String? by project
        val graphpath: String? by project
        val targetdirectories: String? by project
        args = listOfNotNull("extractDependencies", deplevel, projectpath, graphpath, targetdirectories)
        jvmArgs = listOf("-Xmx8g", "-Djava.awt.headless=true")
//        jvmArgs = listOf("-Xmx8g")
    }

    register("extractDependencies") {
        dependsOn(runIde)
    }
}

tasks.register<RunIdeTask>("importProject"){
    val projectpath: String? by project
    args = listOfNotNull("importProject", projectpath)
//    jvmArgs = listOf("-Xmx8g", "-Djava.awt.headless=true")
    jvmArgs = listOf("-Xmx8g")
}