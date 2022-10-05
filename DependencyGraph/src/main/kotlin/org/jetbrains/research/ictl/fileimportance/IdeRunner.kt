package org.jetbrains.research.ictl.fileimportance

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.*
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.searches.ReferencesSearch
import kotlinx.serialization.ExperimentalSerializationApi
import org.jetbrains.kotlin.idea.base.utils.fqname.getKotlinFqName
import org.jetbrains.research.ictl.csv.CSVFormat
import java.io.File
import kotlin.system.exitProcess

class IdeRunner : ApplicationStarter {

    override fun getCommandName(): String = "mine-dependencies"

    @OptIn(ExperimentalSerializationApi::class)
    override fun main(args: MutableList<String>) {
        log(
            "\n    ____                            __                         __  ____                \n" +
                    "   / __ \\___  ____  ___  ____  ____/ /__  ____  _______  __   /  |/  (_)___  ___  _____\n" +
                    "  / / / / _ \\/ __ \\/ _ \\/ __ \\/ __  / _ \\/ __ \\/ ___/ / / /  / /|_/ / / __ \\/ _ \\/ ___/\n" +
                    " / /_/ /  __/ /_/ /  __/ / / / /_/ /  __/ / / / /__/ /_/ /  / /  / / / / / /  __/ /    \n" +
                    "/_____/\\___/ .___/\\___/_/ /_/\\__,_/\\___/_/ /_/\\___/\\__, /  /_/  /_/_/_/ /_/\\___/_/     \n" +
                    "          /_/                                     /____/                               "
        )

        val (dependencyType, projectPath, graphFile, infoFile, targetDirectories) = Args.parse(args)

        val project = ProjectUtil.openOrImport(projectPath)
        if (project == null) {
            log("Could not open the project $projectPath")
            exitProcess(1)
        }

        log("Indexing project ${project.name}")

        val dumbService = project.getService(DumbService::class.java)
        if (dumbService == null) {
            log("Could not get DumbService")
            exitProcess(1)
        }

        val dependencyPsiClass = when (dependencyType) {
            DependencyType.CLASS -> PsiClass::class.java
            DependencyType.METHOD -> PsiMethod::class.java
        }

        dumbService.runWhenSmart {
            log("Indexing has finished")

            val classesWriter = infoFile.bufferedWriter()
            val edgesWriter = graphFile.bufferedWriter()
            var nothingWritten = true

            exportTargetDirectories(project, targetDirectories)

            getAllJavaPsiFiles(project)
                .getAllPsiElements(dependencyPsiClass)
                .onEachIndexed { index, psiElement ->
                    psiElement.toFileInformation()?.let {
                        classesWriter.append(
                            CSVFormat.encodeToString(
                                it,
                                nothingWritten // index == 0
                            )
                        )
                        nothingWritten = false
                    }
                    if (index % 1000 == 0) {
                        log("Written $index elements")
                    }
                }
                .buildDependencyEdges()
                .forEachIndexed { index, dependencyEdge ->
                    edgesWriter.append(CSVFormat.encodeToString(dependencyEdge, index == 0))
                    if (index % 1000 == 0) {
                        log("Written $index edges")
                    }
                }


            exitProcess(0)
        }
    }

    private fun exportTargetDirectories(project: Project, targetDirectories: File) {
        log("exporting target path")

        val projectDir = project.guessProjectDir()
        if (projectDir == null) {
            log("Can not find root project dir")
            return
        }

        val projectPrefix = "${projectDir.path}/"
        val lookupList = mutableListOf(projectDir)
        val directories = mutableListOf<String>()
        while (lookupList.isNotEmpty()) {
            val file = lookupList.removeLast() // works the same way the pop() does
            if (file.isDirectory) {
                directories.add(file.path.replace(projectPrefix, ""))
                if (file.children != null) {
                    lookupList.addAll(file.children)
                }
            }
        }

        log("writing ${directories.size} target directories")
        val writer = targetDirectories.bufferedWriter()
        directories.forEach { writer.appendLine(it) }
        writer.flush()
        writer.close()
    }

    private fun getAllJavaPsiFiles(project: Project) = sequence<PsiFile> {
        log("getAllJavaPsiFiles")
        val psiManager = PsiManager.getInstance(project)

        FilenameIndex.getAllFilesByExt(project, "java").forEach {
            val file = psiManager.findFile(it)
            // yieldNotNull does causes java.lang.ClassNotFoundException: org.jetbrains.kotlin.utils.CollectionsKt
            if (file != null) {
                yield(file)
            }
        }
    }

    private fun PsiElement.toPsiClass() = when (this) {
        is PsiClass -> this
        is PsiMethod -> this.containingClass
        else -> error("Unrecognized PsiElement: ${this.getKotlinFqName()}")
    }

    private fun PsiElement.toFileInformation() = toPsiClass()?.let {
        FileInformation(
            // TODO: suspicious, too many local/anonymous
            it.qualifiedName ?: anonymousName,
            it.containingFile.virtualFile.presentableName
        )
    }

    private fun PsiElement.getFileName() = containingFile.virtualFile.presentableName

    private fun Sequence<PsiElement>.buildDependencyEdges() =
        flatMap { psiElement ->
            ReferencesSearch
                .search(psiElement)
                .asSequence()
                .filterNot {
                    it.element.containingFile.isEquivalentTo(psiElement.containingFile)
                }
                .map {
                    DependencyEdge(it.element.getFileName(), psiElement.getFileName())
                }
        }

    private fun <T> Sequence<PsiFile>.getAllPsiElements(dependencyPsiClass: Class<T>) =
        flatMap { psiFile ->
            val elements = mutableListOf<PsiElement>()
            psiFile.acceptChildren(object : JavaRecursiveElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (dependencyPsiClass.isAssignableFrom(element::class.java)) {
                        elements.add(element)
                    }

                    super.visitElement(element)
                }
            })
            elements
        }

    companion object {
        const val anonymousName = "Some Local/Anonymous class"

        inline fun <reified T> log(log: T) {
            println("****Miner**** $log")
        }
    }
}
