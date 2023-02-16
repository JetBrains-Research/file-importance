package org.jetbrains.research.ictl.fileimportance

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

abstract class DependencyExtractor(
    val project: Project,
    val args: ExportDependenciesArgs
) {

    abstract fun extractEdges(): Sequence<DependencyEdge>
    abstract fun getAllFiles(): Sequence<PsiFile>

    abstract fun prepare()

    protected fun PsiElement.getFileName() = containingFile?.virtualFile?.getFileName() ?: ""
    protected fun VirtualFile.getFileName() = path.replace("${args.projectPath}/", "")
}
