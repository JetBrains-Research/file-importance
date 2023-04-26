package org.jetbrains.research.ictl.fileimportance

import com.intellij.ide.impl.OpenProjectTask
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.actionSystem.ex.ActionManagerEx
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.testFramework.closeProjectAsync
import com.intellij.vcs.log.impl.VcsProjectLog
import org.jetbrains.research.ictl.csv.CSVFormat
import kotlin.system.exitProcess
import kotlinx.serialization.ExperimentalSerializationApi

class ExportDependenciesRunner : ApplicationStarter {
    @Deprecated("Specify it as `id` for extension definition in a plugin descriptor")
    override val commandName: String
        get() = "extractDependencies"

    @OptIn(ExperimentalSerializationApi::class)
    override fun main(args: List<String>) {
        log(Utils.BANNER)

        val exportDependenciesArgs = ExportDependenciesArgs.parse(args)


        val project = exitOnNull(ProjectUtil.openOrImport(exportDependenciesArgs.projectPath)) {
            "Could not open the project ${exportDependenciesArgs.projectPath}"
        }

        log("Indexing project ${project.name}")

        val dumbService = exitOnNull(project.getService(DumbService::class.java)) {
            "Could not get DumbService"
        }

        dumbService.runWhenSmart {
            log("Indexing has finished")
            VcsProjectLog.runWhenLogIsReady(project) {
                try {
                    buildGraph(project, exportDependenciesArgs)
                } catch (e: Exception) {
                    e.printStackTrace()
                    exitProcess(1)
                }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun buildGraph(
        project: Project,
        args: ExportDependenciesArgs
    ) {
        val dependencyExtractors = listOf(
//            DependencyExtractor(project, "java", args.projectPath),
            DependencyExtractor(project, args.projectPath)
        )

        TargetExtractor(dependencyExtractors, args.targetDirectories).exportTargetDirectories()

        dependencyExtractors.forEach { it.prepare() }

        var header = true
        var edgeCount = 0
        args.graphFile.bufferedWriter().use { edgesWriter ->
            dependencyExtractors.forEach { extractor ->
                extractor
                    .extractEdges()
                    .forEach { dependencyEdge ->
                        edgesWriter.append(CSVFormat.encodeToString(dependencyEdge, header))
                        header = false

                        if(edgeCount % 1000 == 0){
                            log("Found $edgeCount edges")
                        }

                        edgeCount++
                    }
            }
        }

        exitProcess(0)
    }
}
