package com.jetbrains.research.ictl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import java.util.Properties
import java.util.concurrent.TimeUnit
import java.lang.ProcessBuilder.Redirect

class MainTest {
    companion object {
        val properties = Properties()

        @JvmStatic
        @BeforeAll
        fun checkThatTestRepoExists() {
            File("./gradle.properties")
                .reader().use {
                    properties.load(it)
                }

            val testRepoFile = File(properties.getProperty("testrepo.folder"))
            assertTrue(testRepoFile.exists() && testRepoFile.isDirectory)
        }
    }

    @Test
    fun run() {
        val file = File("./testrepo")
        if (file.exists()) {
            println("IT EXISTS!")
        }
        ProcessBuilder("./gradlew",  "-p", "../DependencyGraph", "extractDependencies", "-Pprojectpath=${file.absolutePath}",  "-Pgraphpath='./out/graph.csv'",  "-Ptargetdirectories='./out/targets.txt'")
            .redirectOutput(Redirect.INHERIT)
            .redirectError(Redirect.INHERIT)
            .start()
            .waitFor(60, TimeUnit.MINUTES)
        assertTrue(true)
    }
}
