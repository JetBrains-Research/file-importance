package org.jetbrains.research.ictl.fileimportance

import com.intellij.util.io.exists
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.system.exitProcess

data class ExportDependenciesArgs(
    val projectPath: Path,
    val graphFile: File,
    val targetDirectories: File
) {
    companion object {
        fun parse(args: List<String>): ExportDependenciesArgs {

            val projectPath = Path(args[1])
            if (!projectPath.exists()) {
                log("Path $projectPath does not exist")
                exitProcess(1)
            }

            val graphFile = Paths.get(args[2]).toFile()
            if (!graphFile.exists() && !graphFile.createNewFile()) {
                log("Could not create ${graphFile.path} does not exist")
                exitProcess(1)
            }

            val targetDirectories = File(args[3])
            if (!targetDirectories.exists() && !targetDirectories.createNewFile()) {
                log("Could not create ${targetDirectories.path} does not exist")
                exitProcess(1)
            }

            return ExportDependenciesArgs(projectPath, graphFile, targetDirectories)
        }
    }
}