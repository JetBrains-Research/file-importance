import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.9.0"
    kotlin("jvm") version "1.7.10"
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
    implementation("com.google.code.gson:gson:2.9.0")
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
        val deplevel: String? by project
        val projectpath: String? by project
        val graphpath: String? by project
        val infopath: String? by project
        args = listOfNotNull("mine-dependencies", deplevel, projectpath, graphpath, infopath)
        jvmArgs = listOf("-Djava.awt.headless=true")
    }

    register("extractDependencies") {
        dependsOn(runIde)
    }
}