package org.jetbrains.research.ictl.fileimportance

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch

abstract class IDependencyExtractor(protected val project: Project) {

    abstract fun extractEdges(): Sequence<DependencyEdge>
    abstract fun getAllFiles(): Sequence<PsiFile>

    protected fun Sequence<PsiElement>.buildDependencyEdges() = flatMap { psiElement ->
        ReferencesSearch.search(psiElement, GlobalSearchScope.everythingScope(project), false)
            .asSequence()
            .filterNot {
                it.element.containingFile.isEquivalentTo(psiElement.containingFile)
            }.map {
                DependencyEdge(it.element.getFileName(), psiElement.getFileName())
            }
    }
}