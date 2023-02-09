package org.jetbrains.research.ictl.fileimportance

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import kotlinx.serialization.ExperimentalSerializationApi
import org.jetbrains.research.ictl.csv.CSVFormat
import java.util.*
import kotlin.system.exitProcess

private const val s = ""

class ExportDependenciesRunner : ApplicationStarter {

    override fun getCommandName(): String = "extractDependencies"

    @OptIn(ExperimentalSerializationApi::class)
    override fun main(args: MutableList<String>) {
        log(Utils.BANNER)

        ARGS = ExportDependenciesArgs.parse(args)

        val project = ProjectUtil.openOrImport(ARGS.projectPath)
        if (project == null) {
            log("Could not open the project ${ARGS.projectPath}")
            exitProcess(1)
        }

        log("Indexing project ${project.name}")

        val dumbService = project.getService(DumbService::class.java)
        if (dumbService == null) {
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

    private fun buildGraph(
        project: Project,
    ) {

        val dependencyExtractors = listOf(
            JavaDependencyExtractor(project),
            KotlinDependencyExtractor(project)
        )

        val targetExtractor = TargetExtractor(project, dependencyExtractors)
        exportTargetDirectories(targetExtractor)


        val edgesWriter = ARGS.graphFile.bufferedWriter()

        dependencyExtractors.forEach { it.prepare() }
        var header = true
        dependencyExtractors.forEachIndexed { extractorIndex, extractor ->
            extractor
                .extractEdges()
                .forEachIndexed { index, dependencyEdge ->
                    edgesWriter.append(CSVFormat.encodeToString(dependencyEdge, header))
                    header = false
                    if (index % 1000 == 0) {
                        log("Written $index edges")
                    }
                }
        }

        edgesWriter.flush()
        edgesWriter.close()

//        var path = ARGS.graphFile.absolutePath
//        path = path.substring(0, path.lastIndexOf("/") + 1)
//        path += "features.csv"
//
//        val edgesWriter = File(path).bufferedWriter()
//        var header = true
//        edgesWriter.append(",equal\n")
//        getPsiFiles(project, "java")
//            .forEach {
//                edgesWriter.append(CSVFormat.encodeToString(Features(it.getFileName(), 1.0), false))
//                header = false
//            }
//
//        getPsiFiles(project, "kt")
//            .forEach {
//                edgesWriter.append(CSVFormat.encodeToString(Features(it.getFileName(), 1.0), false))
//                header = false
//            }

        exitProcess(0)
    }

    private fun exportTargetDirectories(extractor: TargetExtractor) {
        val targets = extractor.extract() ?: return

        log("writing target directories")
        ARGS.targetDirectories
            .bufferedWriter()
            .use { writer ->
                targets.forEach { writer.appendLine(it) }
            }
    }


    companion object {
        lateinit var ARGS: ExportDependenciesArgs
        const val anonymousName = "Some Local/Anonymous class"
    }
}
