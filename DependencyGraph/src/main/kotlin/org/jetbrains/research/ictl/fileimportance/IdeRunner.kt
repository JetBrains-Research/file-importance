package org.jetbrains.research.ictl.fileimportance

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.searches.ReferencesSearch
import kotlinx.serialization.ExperimentalSerializationApi
import org.jetbrains.research.ictl.csv.CSVFormat
import java.io.File
import kotlin.system.exitProcess

private const val s = ""

class IdeRunner : ApplicationStarter {

    private lateinit var ARGS: Args

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

        ARGS = Args.parse(args)
        val (dependencyType, projectPath, graphFile, infoFile, targetDirectories) = ARGS

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

            val edgesWriter = graphFile.bufferedWriter()
            var nothingWritten = true

            exportTargetDirectories(project, targetDirectories)

            getAllJavaPsiFiles(project)
                .getAllPsiElements(dependencyPsiClass)
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

        val directories = HashMap<String, Long>()
        val rootDirectory = ""
        directories[rootDirectory] = 0

        // Finding the directories of intrest
        val lookupList = projectDir.children.toMutableList()
        while (lookupList.isNotEmpty()) {
            val file = lookupList.last()
            lookupList.remove(file)
            if (file.isDirectory) {
                // Check if the directory only contains another directory
                if (file.children.size != 1 || !file.children[0].isDirectory) {
                    directories[file.getFileName()] = 0
                }

                if (file.children != null) {
                    lookupList.addAll(file.children)
                }
            }
        }

        // Find out number of characters in java files under any directory of interest
        for (file in getAllJavaPsiFiles(project)) {
            val count = file.textLength
            var parentPath = file.virtualFile.getFileName()
            while (parentPath.contains("/")){
                parentPath = parentPath.substring(0, parentPath.lastIndexOf("/"))
                if (directories.containsKey(parentPath)) {
                    directories[parentPath] = directories[parentPath]!! + count
                }
            }

            directories[rootDirectory] = directories[rootDirectory]!! + count
        }

        log("writing target directories")
        targetDirectories.bufferedWriter().use { writer ->

            // Select top 5% directories
            directories
                .toList()
                .sortedByDescending { it.second }
                .take(directories.size/20)
                .map { it.first }
                .forEach { writer.appendLine(it) }
        }
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

    private fun PsiElement.getFileName() = containingFile.virtualFile.getFileName()
    private fun VirtualFile.getFileName() = path.replace("${ARGS.projectPath.toString()}/", "")

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
