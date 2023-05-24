package com.jetbrains.research.ictl

import org.junit.jupiter.api.Assertions
import java.io.File

class Utils {
    companion object{
        fun assertFileExists(file: File, name: String){
            Assertions.assertTrue(file.exists(), "$name file does not exists ${file.path}")
        }
    }
}