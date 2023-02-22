package com.jetbrains.research.ictl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File

class MainTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun checkThatTestRepoExists() {
            val testRepoFile = File("./testrepo")
            assertTrue(testRepoFile.exists() && testRepoFile.isDirectory)
        }
    }

    @Test
    fun notest() {
        assertTrue(true)
    }
}
