package com.jetbrains.research.ictl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.Properties
import java.util.concurrent.TimeUnit
import java.lang.ProcessBuilder.Redirect
import kotlin.system.exitProcess

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

        @JvmStatic
        fun provideFilenames() = listOf(
            // filename1, filename2
            Arguments.of("graph.csv"),
            Arguments.of("targets.txt"),
        )
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

    @ParameterizedTest
    @MethodSource("provideFilenames")
    fun compareResults(filename: String) {
        val file1 = MainTest::class.java.getResource("/out1/$filename")?.let { File(it.file) }
        val file2 = MainTest::class.java.getResource("/out2/$filename")?.let { File(it.file) }
        if (file1 == null || file2 == null) {
            assertNotNull(file1)
            assertNotNull(file2)
            exitProcess(1)
        }
        val contents1 = file1.readLines()
        val contents2 = file2.readLines()
        assertEquals(contents1.size, contents2.size)
        val set1 = contents1.toSet()
        val set2 = contents1.toSet()
        set2.forEach {
            assertTrue(it in set1) { "$it not present in model output" }
        }
        set2.forEach {
            assertTrue(it in set1) { "$it not present in model output" }
        }
    }
}
