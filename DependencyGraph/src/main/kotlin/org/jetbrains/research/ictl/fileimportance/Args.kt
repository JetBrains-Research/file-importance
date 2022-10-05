package org.jetbrains.research.ictl.fileimportance

import com.intellij.util.io.exists
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.system.exitProcess

// TODO: switch to proper args parser or write it
data class Args(
    val dependencyType: DependencyType,
    val projectPath: Path,
    val graphFile: File,
    val infoFile: File,
    val targetDirectories: File
) {
    companion object {
        private const val argsCount = 6 // name + 5

        fun parse(args: List<String>): Args {
            require(args.size == argsCount) { "Expecting $argsCount arguments, got ${args.size} instead" }

            val dependencyType = try {
                DependencyType.valueOf(args[1])
            } catch (e: IllegalArgumentException) {
                IdeRunner.log("Can not find dependency type ${args[1]}")
                exitProcess(1)
            }

            val projectPath = Path(args[2])
            require(projectPath.exists()) { "Path $projectPath does not exist" }

            val graphFile = Paths.get(args[3]).toFile()
            require(graphFile.exists() || graphFile.createNewFile()) { "Could not create ${graphFile.path}" }

            val infoFile = File(args[4])
            require(infoFile.exists() || infoFile.createNewFile()) { "Could not create ${infoFile.path}" }

            val targetDirectories = File(args[5])
            require(targetDirectories.exists() || targetDirectories.createNewFile()) { "Could not create ${targetDirectories.path}" }

            return Args(dependencyType, projectPath, graphFile, infoFile, targetDirectories)
        }
    }
}