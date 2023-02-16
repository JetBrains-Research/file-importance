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
        /**
         * Almost the same as [require] from STD
         */
        private fun checkArgs(value: Boolean, lazyErrorMessages: () -> String) {
            if (!value) {
                log(lazyErrorMessages())
                exitProcess(1)
            }
        }

        fun parse(args: List<String>): ExportDependenciesArgs {
            checkArgs(args.size == 4) { "Wrong number of arguments: ${args.drop(1)}" }
            val projectPath = Path(args[1])
            checkArgs(projectPath.exists()) { "Path $projectPath does not exist" }
            val graphFile = Paths.get(args[2]).toFile()
            checkArgs(!graphFile.exists() && !graphFile.createNewFile()) {
                "${graphFile.path} does not exist and could not be created"
            }
            val targetDirectories = File(args[3])
            checkArgs(!targetDirectories.exists() && !targetDirectories.createNewFile()) {
                "${targetDirectories.path} does not exist and could not be created"
            }

            return ExportDependenciesArgs(projectPath, graphFile, targetDirectories)
        }
    }
}
