package com.jetbrains.research.ictl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import java.util.Properties

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
    fun notest() {
        assertTrue(true)
    }
}
