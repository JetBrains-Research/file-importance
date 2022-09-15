package org.jetbrains.research.ictl.fileimportance

import com.intellij.util.io.exists
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.system.exitProcess

data class Args(val dependencyType: DependencyType, val projectPath: Path, val graphFile: File, val infoFile: File) {
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

            return Args(dependencyType, projectPath, File(args[3]), File(args[4]))
        }
    }
}