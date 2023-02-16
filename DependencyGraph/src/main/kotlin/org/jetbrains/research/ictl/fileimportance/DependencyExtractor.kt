package org.jetbrains.research.ictl.fileimportance

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

abstract class DependencyExtractor(protected val project: Project) {

    abstract fun extractEdges(): Sequence<DependencyEdge>
    abstract fun getAllFiles(): Sequence<PsiFile>

    abstract fun prepare()
}
