package org.jetbrains.research.ictl.fileimportance

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.refactoring.suggested.startOffset
import java.nio.file.Path

class DependencyExtractor(
    val project: Project,
    val extension: String,
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
                    log(e.message)
                } finally {
                    yieldAll(edgesBuffer)
                    edgesBuffer.clear()
                }
            }
    }

    override fun visitElement(element: PsiElement) {
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
    }

    private fun PsiElement.getFileName() = containingFile?.virtualFile?.getFileName() ?: ""
    private fun VirtualFile.getFileName() = path.replace("${projectPath}/", "")

    fun getAllPsiFiles() = sequence {
        log("get all $extension files")

        FilenameIndex.getAllFilesByExt(project, extension, GlobalSearchScope.projectScope(project)).forEach {
            val file = psiManager.findFile(it)
            // yieldNotNull raises java.lang.ClassNotFoundException: org.jetbrains.kotlin.utils.CollectionsKt
            file?.let { yield(file) }
        }
    }

    fun prepare() {
        getAllPsiFiles().forEach { it.acceptChildren(DumbVisitor) }
    }

    private object DumbVisitor : JavaRecursiveElementVisitor()
}