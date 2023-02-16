package org.jetbrains.research.ictl.fileimportance

import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Path

class TargetExtractor(private val dependencyExtractors: List<DependencyExtractor>) {
    fun extract(): List<String>? {
        log("exporting target paths")

        exitOnFalse(dependencyExtractors.isNotEmpty()) { "No dependency extractors" }

        val projectDir = dependencyExtractors.first().project.guessProjectDir() ?: run {
            log("Can not find root project dir")
            return null
        }
        val projectPath = dependencyExtractors.first().args.projectPath

        val directories = HashMap<String, Long>()
        val rootDirectory = ""
        directories[rootDirectory] = 0

        // Find directories of interest
        val lookupList = projectDir.children.toMutableList()
        while (lookupList.isNotEmpty()) {
            val file = lookupList.removeLast()
            if (file.isDirectory) {
                // Check if the directory only contains another directory
                if (file.children.size != 1 || !file.children[0].isDirectory) {
                    directories[file.getFileName(projectPath)] = 0
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
            var parentPath = file.virtualFile.getFileName(projectPath)
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
        dependencyExtractors.first().args.targetDirectories
            .bufferedWriter()
            .use { writer ->
                targets.forEach { writer.appendLine(it) }
            }
    }

    private fun VirtualFile.getFileName(projectPath: Path) = path.replace("${projectPath}/", "")
}
