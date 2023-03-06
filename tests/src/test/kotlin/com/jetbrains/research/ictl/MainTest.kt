package com.jetbrains.research.ictl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.*
import com.jetbrains.research.ictl.Utils.Companion.assertFileExists
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import net.javacrumbs.jsonunit.assertj.assertThatJson
import net.javacrumbs.jsonunit.core.Option
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.`is`

class MainTest() {
    companion object {
        val properties = Properties()
        lateinit var actualPath: String
        lateinit var expectedPath: String

        @JvmStatic
        @BeforeAll
        fun checkThatTestRepoExists() {
            File("./gradle.properties")
                .reader().use {
                    properties.load(it)
                }

            actualPath = properties.getProperty("outputs.actual")
            expectedPath = properties.getProperty("outputs.expected")

            val actualFilesDirectory = File(actualPath)
            val expectedFilesDirectory = File(expectedPath)

            assertFileExists(actualFilesDirectory, "Actual files folder")
            assertFileExists(expectedFilesDirectory, "Expected files folder")
        }

        @JvmStatic
        fun provideFilenames() = listOf(
            // filename1, filename2
            Arguments.of("authorships.json"),
            Arguments.of("avelinoBFResults.json"),
            Arguments.of("features.csv"),
            Arguments.of("graph.csv"),
            Arguments.of("jetbrainsBFResults.json"),
            Arguments.of("targets.txt"),
//            Arguments.of("avelino_alias.txt"),
//            Arguments.of("jetbrains_merge.json"),
//            Arguments.of("users.json"),
        )
    }

//    @Test
//    fun run() {
//        val file = File("./testrepo")
//        if (file.exists()) {
//            println("IT EXISTS!")
//        }
//        ProcessBuilder(
//            "./gradlew",
//            "-p",
//            "../DependencyGraph",
//            "extractDependencies",
//            "-Pprojectpath=${file.absolutePath}",
//            "-Pgraphpath='./out/graph.csv'",
//            "-Ptargetdirectories='./out/targets.txt'"
//        )
//            .redirectOutput(Redirect.INHERIT)
//            .redirectError(Redirect.INHERIT)
//            .start()
//            .waitFor(60, TimeUnit.MINUTES)
//        assertTrue(true)
//    }

    @ParameterizedTest
    @MethodSource("provideFilenames")
    fun compareResults(filename: String) {
        println("Evaluating $filename")

        val actualFile = File("$actualPath/$filename")
        val expectedFile = File("$expectedPath/$filename")

        assertFileExists(actualFile, "Actual")
        assertFileExists(expectedFile, "Expected")

        when {
            filename.contains(".json") -> {
                compareJsonFiles(actualFile, expectedFile)
            }
            else -> {
                comparePlainText(actualFile, expectedFile)
            }
        }
    }

    private fun comparePlainText(actualFile: File, expectedFile: File) {
        var actualData = actualFile.readLines()
        var expectedData = expectedFile.readLines()
        assertEquals(actualData.size, expectedData.size, "Data size is not equal")

        actualData = actualData.sorted()
        expectedData = expectedData.sorted()
        assertArrayEquals(actualData.toTypedArray(), expectedData.toTypedArray(), "Content of ${expectedFile.path} and ${actualFile.path} is not equal")
    }

    private fun compareJsonFiles(actualFile: File, expectedFile: File) {
        val actualContent = actualFile.readText()
        val expectedContent = expectedFile.readText()

        assertThatJson(actualContent)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(expectedContent)
            .withFailMessage("Json content is not equal for ${actualFile.path} -> ${expectedFile.path}")

    }
}
