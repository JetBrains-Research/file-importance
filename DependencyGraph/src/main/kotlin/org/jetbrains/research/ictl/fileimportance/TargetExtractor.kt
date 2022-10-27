package org.jetbrains.research.ictl.fileimportance

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir

class TargetExtractor(private val project: Project, private val dependencyExtractors: List<IDependencyExtractor>) {

    fun extract(): List<String>? {
        log("exporting target path")

        val projectDir = project.guessProjectDir()
        if (projectDir == null) {
            log("Can not find root project dir")
            return null
        }

        val directories = HashMap<String, Long>()
        val rootDirectory = ""
        directories[rootDirectory] = 0

        // Finding the directories of intrest
        val lookupList = projectDir.children.toMutableList()
        while (lookupList.isNotEmpty()) {
            val file = lookupList.last()
            lookupList.remove(file)
            if (file.isDirectory) {
                // Check if the directory only contains another directory
                if (file.children.size != 1 || !file.children[0].isDirectory) {
                    directories[file.getFileName()] = 0
                }

                if (file.children != null) {
                    lookupList.addAll(file.children)
                }
            }
        }

//         Find out number of characters in java files under any directory of interest
        val files = dependencyExtractors.flatMap { it.getAllFiles() }
        for (file in files) {
            val count = file.textLength
            var parentPath = file.virtualFile.getFileName()
            while (parentPath.contains("/")) {
                parentPath = parentPath.substring(0, parentPath.lastIndexOf("/"))
                if (directories.containsKey(parentPath)) {
                    directories[parentPath] = directories[parentPath]!! + count
                }
            }

            directories[rootDirectory] = directories[rootDirectory]!! + count
        }

        // Select top 5% directories
        var selectTargetCount = directories.size / 20
        if (selectTargetCount == 0){
            selectTargetCount = 5
        }

        return directories.toList().sortedByDescending { it.second }.take(selectTargetCount).map { it.first }
    }
}