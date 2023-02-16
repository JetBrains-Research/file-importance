package org.jetbrains.research.ictl.fileimportance

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import org.jetbrains.research.ictl.csv.CSVFormat
import kotlin.system.exitProcess
import kotlinx.serialization.ExperimentalSerializationApi

class ExportDependenciesRunner : ApplicationStarter {
    override fun getCommandName(): String = "extractDependencies"

    @OptIn(ExperimentalSerializationApi::class)
    override fun main(args: MutableList<String>) {
        log(Utils.BANNER)

        ARGS = ExportDependenciesArgs.parse(args)

        val project = ProjectUtil.openOrImport(ARGS.projectPath)
        project ?: run {
            log("Could not open the project ${ARGS.projectPath}")
            exitProcess(1)
        }

        log("Indexing project ${project.name}")

        val dumbService = project.getService(DumbService::class.java)
        dumbService ?: run {
            log("Could not get DumbService")
            exitProcess(1)
        }

        dumbService.runWhenSmart {
            log("Indexing has finished")
            try {
                buildGraph(project)
            } catch (e: Exception) {
                e.printStackTrace()
                exitProcess(1)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun buildGraph(
        project: Project,
    ) {
        val dependencyExtractors = listOf(
            JavaDependencyExtractor(project),
            KotlinDependencyExtractor(project)
        )

        TargetExtractor(project, dependencyExtractors).exportTargetDirectories()

        dependencyExtractors.forEach { it.prepare() }

        ARGS.graphFile.bufferedWriter().use { edgesWriter ->
            dependencyExtractors.forEachIndexed { extractorIndex, extractor ->
                extractor
                    .extractEdges()
                    .forEachIndexed { index, dependencyEdge ->
                        edgesWriter.append(CSVFormat.encodeToString(dependencyEdge, index == 0))
                        if ((index + 1) % 1000 == 0) {
                            log("Written $index edges")
                        }
                    }
            }
        }

        exitProcess(0)
    }

    companion object {
        lateinit var ARGS: ExportDependenciesArgs
    }
}
