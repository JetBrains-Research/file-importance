package org.jetbrains.research.ictl.fileimportance

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
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

        var header = true
        ARGS.graphFile.bufferedWriter().use { edgesWriter ->
            dependencyExtractors.forEach { extractor ->
                extractor
                    .extractEdges()
                    .forEach { dependencyEdge ->
                        edgesWriter.append(CSVFormat.encodeToString(dependencyEdge, header))
                        header = false
                    }
            }
        }

        exitProcess(0)
    }

    companion object {
        lateinit var ARGS: ExportDependenciesArgs
    }
}
