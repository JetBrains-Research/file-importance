package org.jetbrains.research.ictl.fileimportance

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.util.io.exists
import kotlinx.serialization.ExperimentalSerializationApi
import org.jetbrains.research.ictl.csv.CSVFormat
import kotlin.io.path.Path
import kotlin.system.exitProcess

class ImportProjectRunner : ApplicationStarter {

    override fun getCommandName(): String = "importProject"

    @OptIn(ExperimentalSerializationApi::class)
    override fun main(args: MutableList<String>) {
        log(Utils.BANNER)

        val projectPath = Path(args[1])
        if (!projectPath.exists()) {
            log("Path $projectPath does not exist")
            exitProcess(1)
        }

        val project = ProjectUtil.openOrImport(projectPath)
        if (project == null) {
            log("Could not open the project ${projectPath}")
            exitProcess(1)
        }

        log("Indexing project ${project.name}")
    }


}