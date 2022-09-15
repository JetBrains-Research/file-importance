package org.jetbrains.research.ictl.fileimportance

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.searches.ReferencesSearch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.File
import kotlin.system.exitProcess

class IdeRunner : ApplicationStarter {

    override fun getCommandName(): String = "mine-dependencies"
    override fun main(args: MutableList<String>) {
        log(
            "\n    ____                            __                         __  ____                \n" +
                    "   / __ \\___  ____  ___  ____  ____/ /__  ____  _______  __   /  |/  (_)___  ___  _____\n" +
                    "  / / / / _ \\/ __ \\/ _ \\/ __ \\/ __  / _ \\/ __ \\/ ___/ / / /  / /|_/ / / __ \\/ _ \\/ ___/\n" +
                    " / /_/ /  __/ /_/ /  __/ / / / /_/ /  __/ / / / /__/ /_/ /  / /  / / / / / /  __/ /    \n" +
                    "/_____/\\___/ .___/\\___/_/ /_/\\__,_/\\___/_/ /_/\\___/\\__, /  /_/  /_/_/_/ /_/\\___/_/     \n" +
                    "          /_/                                     /____/                               "
        )

        val (dependencyType, projectPath, graphFile, infoFile) = Args.parse(args)

        val project = ProjectUtil.openOrImport(projectPath)
        if (project == null) {
            log("Could not open the project $projectPath")
            exitProcess(1)
        }

        val dumbService = project.getService(DumbService::class.java)
        if (dumbService == null) {
            log("Could not get DumbService")
            exitProcess(1)
        }

        dumbService.runWhenSmart {
            log("Indexing has finished")

            val psiFiles = getAllRelatedFiles(project)

            val elements = getAllRelatedElements(psiFiles, dependencyType)
            exportClasses(elements, infoFile)

            val edges = buildDependencyGraph(elements)
            writeToJson(edges, graphFile)

            exitProcess(0)
        }
    }

    private fun PsiElement.toClass() = when (this) {
        is PsiClass -> this
        is PsiMethod -> this.containingClass
        else -> {
            log("Unrecognized PsiElement")
            null
            // Why not `this.getContainingClass()`?
        }
    }

    private fun exportClasses(elements: List<PsiElement>, infoFile: File) {
        log("Exporting class information tp ${infoFile.absolutePath}")

        val result = elements
            .mapNotNull { it.toClass() }
            .map { psiClass ->
                FileInformation(
                    psiClass.qualifiedName ?: "Some Local/Anonymous class",
                    psiClass.containingFile.virtualFile.presentableName
                )
            }

        writeToJson(result, infoFile)
    }

    private fun PsiElement.getFileName() = containingFile.virtualFile.presentableName

    private fun buildDependencyGraph(elements: List<PsiElement>): List<DependencyEdge> {
        var lastCheckpoint = 0 // DEBUG
        var currSize = 0 // DEBUG
        log("Building a graph for ${elements.size} elements")

        return elements.flatMap { psiElement ->
            ReferencesSearch
                .search(psiElement)
                .map { psiReference ->
                    DependencyEdge(psiReference.element.getFileName(), psiElement.getFileName())
                }.also {
                    currSize += it.size
                    if (currSize - lastCheckpoint > 1000) {
                        lastCheckpoint = currSize
                        log("Build $lastCheckpoint edges so far")
                    }
                }
        }
    }

    private fun getAllRelatedElements(psiFiles: List<PsiFile?>, dependencyType: DependencyType): List<PsiElement> {
        val clazz = when (dependencyType) {
            DependencyType.CLASS -> PsiClass::class.java
            DependencyType.METHOD -> PsiMethod::class.java
        }

        return getAllPsiElements(psiFiles, clazz)
    }

    private fun getAllRelatedFiles(project: Project): List<PsiFile?> {
        log("Getting All related files")

        val files = FilenameIndex.getAllFilesByExt(project, "java")
        val psiManager = PsiManager.getInstance(project)

        return files.mapNotNull { f -> psiManager.findFile(f) }
    }

    private fun <T> getAllPsiElements(psiFiles: List<PsiFile?>, clazz: Class<T>): List<PsiElement> {
        log("Getting All related elements")
        val elements = mutableListOf<PsiElement>()
        psiFiles.forEach {
            it?.acceptChildren(object : JavaRecursiveElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (clazz.isAssignableFrom(element::class.java)) {
                        elements.add(element)
                    }

                    super.visitElement(element)
                }
            })
        }
        return elements
    }

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        inline fun <reified T> writeToJson(what: T, file: File) = try {
            Json.encodeToStream(what, file.outputStream())
        } catch (e: Exception) {
            log(e.stackTraceToString())
        }

        fun log(log: String) {
            println("****Miner**** $log")
        }
    }
}
