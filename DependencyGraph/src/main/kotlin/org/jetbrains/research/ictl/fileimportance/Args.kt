package org.jetbrains.research.ictl.fileimportance

import com.intellij.util.io.exists
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.system.exitProcess

data class Args(
    val dependencyType: DependencyType,
    val projectPath: Path,
    val graphFile: File,
    val infoFile: File,
    val targetDirectories: File
) {
    companion object {
        fun parse(args: List<String>): Args {
            val dependencyType = try {
                DependencyType.valueOf(args[1])
            } catch (e: IllegalArgumentException) {
                IdeRunner.log("Can not find dependency type ${args[1]}")
                exitProcess(1)
            }

            val projectPath = Path(args[2])
            if (!projectPath.exists()) {
                IdeRunner.log("Path $projectPath does not exist")
                exitProcess(1)
            }

            val graphFile = Paths.get(args[3]).toFile()
            if (!graphFile.exists() && !graphFile.createNewFile()) {
                IdeRunner.log("Could not create ${graphFile.path} does not exist")
                exitProcess(1)
            }

            val infoFile = File(args[4])
            if (!infoFile.exists() && !infoFile.createNewFile()) {
                IdeRunner.log("Could not create ${infoFile.path} does not exist")
                exitProcess(1)
            }

            val targetDirectories = File(args[5])
            if (!targetDirectories.exists() && !targetDirectories.createNewFile()) {
                IdeRunner.log("Could not create ${targetDirectories.path} does not exist")
                exitProcess(1)
            }

            return Args(dependencyType, projectPath, graphFile, infoFile, targetDirectories)
        }
    }
}