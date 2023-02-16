package org.jetbrains.research.ictl.fileimportance

import com.intellij.util.io.exists
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path

data class ExportDependenciesArgs(
    val projectPath: Path,
    val graphFile: File,
    val targetDirectories: File
) {
    companion object {
        fun parse(args: List<String>): ExportDependenciesArgs {
            exitOnFalse(args.size == 4) { "Wrong number of arguments: ${args.drop(1)}" }
            val projectPath = Path(args[1])
            exitOnFalse(projectPath.exists()) { "Path $projectPath does not exist" }
            val graphFile = Paths.get(args[2]).toFile()
            exitOnFalse(graphFile.exists() || graphFile.createNewFile()) {
                "${graphFile.path} does not exist and could not be created"
            }
            val targetDirectories = File(args[3])
            exitOnFalse(targetDirectories.exists() || targetDirectories.createNewFile()) {
                "${targetDirectories.path} does not exist and could not be created"
            }

            return ExportDependenciesArgs(projectPath, graphFile, targetDirectories)
        }
    }
}
