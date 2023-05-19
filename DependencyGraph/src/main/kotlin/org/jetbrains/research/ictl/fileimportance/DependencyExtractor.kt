package org.jetbrains.research.ictl.fileimportance

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import java.nio.file.Path

class DependencyExtractor(
    val project: Project,
    val projectPath: Path
) : PsiRecursiveElementVisitor() {
    private val edgesBuffer: MutableList<DependencyEdge> = mutableListOf()
    private val psiManager = PsiManager.getInstance(project)

    fun extractEdges() = sequence {
        getAllPsiFiles()
            .forEach { psiFile ->
                try {
                    psiFile.acceptChildren(this@DependencyExtractor)
                } catch (e: Exception) {
//                    log(e.message)
                } finally {
                    yieldAll(edgesBuffer)
                    edgesBuffer.clear()
                }
            }
    }

    override fun visitElement(element: PsiElement) {
        try {
            element
                .references
                .filterNotNull()
                .forEach {
                    val resolve = it.resolve()
                    if (resolve != null && psiManager.isInProject(resolve) && resolve.containingFile != null &&
                        !it.element.containingFile.isEquivalentTo(resolve.containingFile)
                    ) {
                        edgesBuffer.add(DependencyEdge(element.getFileName(), resolve.getFileName()))
                    }
                }

            super.visitElement(element)
        }catch (e: Exception){
//            log("Error: ${e.message}")
//            e.printStackTrace()
        }


    }

    private fun PsiElement.getFileName() = containingFile?.virtualFile?.getFileName() ?: ""
    private fun VirtualFile.getFileName() = path.replace("${projectPath}/", "")

    fun getAllPsiFiles(): Sequence<PsiFile> {
        log("get all files")

        val fileIndex = ProjectFileIndex.getInstance(project)
        val vcsManager = ProjectLevelVcsManager.getInstance(project)
        val files = mutableListOf<PsiFile>()


        fileIndex.iterateContent {

            if (!it.isDirectory && !fileIndex.isInLibrary(it) && vcsManager.isFileInContent(it) && !it.name.contains(".gradle.kts")) {
                val file = psiManager.findFile(it)
                // yieldNotNull raises java.lang.ClassNotFoundException: org.jetbrains.kotlin.utils.CollectionsKt

                file?.let { files.add(file) }
            }

            true
        }

        return files.asSequence()
    }

    fun prepare() {
        getAllPsiFiles().forEach {
            try {
//                log(it.getFileName())
                it.acceptChildren(DumbVisitor)
            }catch (e: Exception){

            }

        }
    }

    private object DumbVisitor : JavaRecursiveElementVisitor(){
        override fun visitElement(element: PsiElement) {
            try {
                element.references.toList()

                super.visitElement(element)
            }catch (e: Exception){
//                log("Error: ${e.message}")
//                e.printStackTrace()
            }
        }
    }
}