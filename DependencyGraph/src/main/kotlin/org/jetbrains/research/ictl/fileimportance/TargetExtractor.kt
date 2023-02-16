package org.jetbrains.research.ictl.fileimportance

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir

class TargetExtractor(private val project: Project, private val dependencyExtractors: List<DependencyExtractor>) {
    fun extract(): List<String>? {
        log("exporting target path")

        val projectDir = project.guessProjectDir()
        projectDir ?: run {
            log("Can not find root project dir")
            return null
        }

        val directories = HashMap<String, Long>()
        val rootDirectory = ""
        directories[rootDirectory] = 0

        // Finding the directories of intrest
        val lookupList = projectDir.children.toMutableList()
        while (lookupList.isNotEmpty()) {
            val file = lookupList.removeLast()
            if (file.isDirectory) {
                // Check if the directory only contains another directory
                if (file.children.size != 1 || !file.children[0].isDirectory) {
                    directories[file.getFileName()] = 0
                }

                file.children?.let {
                    lookupList.addAll(it)
                }
            }
        }

        // Find out number of characters in java files under any directory of interest
        val files = dependencyExtractors.flatMap { it.getAllFiles() }
        for (file in files) {
            val count = file.textLength
            var parentPath = file.virtualFile.getFileName()
            while (parentPath.contains("/")) {
                parentPath = parentPath.substring(0, parentPath.lastIndexOf("/"))
                directories.computeIfPresent(parentPath) { _, prevCount ->
                    prevCount + count
                }?.let { directories[parentPath] = it }
            }

            directories[rootDirectory] = directories[rootDirectory]!! + count
        }

        // Select top 5% directories
        val selectTargetCount = when {
            directories.size >= 1000 -> directories.size / 20
            else -> 50
        }

        return directories.toList()
            .sortedByDescending { it.second }
            .take(selectTargetCount)
            .map { it.first }
    }

    fun exportTargetDirectories() {
        val targets = extract() ?: return

        log("writing target directories")
        ExportDependenciesRunner.ARGS.targetDirectories
            .bufferedWriter()
            .use { writer ->
                targets.forEach { writer.appendLine(it) }
            }
    }
}
