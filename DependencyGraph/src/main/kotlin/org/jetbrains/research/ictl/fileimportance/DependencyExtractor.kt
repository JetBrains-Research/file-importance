package org.jetbrains.research.ictl.fileimportance

import com.intellij.ide.impl.TrustedProjectsStatistics
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.changes.ui.SelectFilesDialog.VirtualFileList
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileFilter
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.refactoring.suggested.startOffset
import org.apache.commons.net.util.TrustManagerUtils
import java.nio.file.Path
import javax.net.ssl.TrustManager

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

    fun getAllPsiFiles(): Sequence<PsiFile> {
        log("get all files")

        val fileIndex = ProjectFileIndex.getInstance(project)
        val vcsManager = ProjectLevelVcsManager.getInstance(project)
        val files = mutableListOf<PsiFile>()


        fileIndex.iterateContent {

            if (!it.isDirectory && !fileIndex.isInLibrary(it) && !vcsManager.isIgnored(it)) {
                val file = psiManager.findFile(it)
                // yieldNotNull raises java.lang.ClassNotFoundException: org.jetbrains.kotlin.utils.CollectionsKt
                file?.let { files.add(file) }
            }

            true
        }

        return files.asSequence()
    }

    fun prepare() {
        getAllPsiFiles().forEach { it.acceptChildren(DumbVisitor) }
    }

    private object DumbVisitor : JavaRecursiveElementVisitor(){
        override fun visitElement(element: PsiElement) {
            element.references.toList()
            super.visitElement(element)
        }
    }
}