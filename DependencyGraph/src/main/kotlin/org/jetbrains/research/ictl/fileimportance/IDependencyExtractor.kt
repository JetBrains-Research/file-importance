package org.jetbrains.research.ictl.fileimportance

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.idea.search.usagesSearch.searchReferencesOrMethodReferences

abstract class IDependencyExtractor(protected val project: Project) {

    abstract fun extractEdges(): Sequence<DependencyEdge>
    abstract fun getAllFiles(): Sequence<PsiFile>

    protected fun Sequence<PsiElement>.buildDependencyEdges() = flatMap { psiElement ->
        psiElement.searchReferencesOrMethodReferences()
            .asSequence()
            .filterNot {
                it.element.containingFile.isEquivalentTo(psiElement.containingFile)
            }.map {
                DependencyEdge(it.element.getFileName(), psiElement.getFileName())
            }
    }

    abstract fun prepare()
}