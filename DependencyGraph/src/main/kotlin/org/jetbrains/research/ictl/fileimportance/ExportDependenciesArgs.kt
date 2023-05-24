package org.jetbrains.research.ictl.fileimportance

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path

data class ExportDependenciesArgs(
    val projectPath: Path,
    val graphFile: File,
    val targetDirectories: File,
    val specialsFile: File
) {
    companion object {
        fun parse(args: List<String>): ExportDependenciesArgs {
            log("Parsing Arguments")

            exitOnFalse(args.size == 5) { "Wrong number of arguments: ${args.drop(1)}" }
            val projectPath = Path(args[1])
            exitOnFalse(projectPath.toFile().exists()) { "Path $projectPath does not exist" }
            val graphFile = Paths.get(args[2]).toFile()
            exitOnFalse(graphFile.exists() || graphFile.createNewFile()) {
                "${graphFile.path} does not exist and could not be created"
            }
            val targetDirectoriesFiles = File(args[3])
            exitOnFalse(targetDirectoriesFiles.exists() || targetDirectoriesFiles.createNewFile()) {
                "${targetDirectoriesFiles.path} does not exist and could not be created"
            }
            val specialsFile = File(args[4])
            exitOnFalse(specialsFile.exists() || specialsFile.createNewFile()) {
                "${specialsFile.path} does not exist and could not be created"
            }
            return ExportDependenciesArgs(projectPath, graphFile, targetDirectoriesFiles, specialsFile)
        }
    }
}
