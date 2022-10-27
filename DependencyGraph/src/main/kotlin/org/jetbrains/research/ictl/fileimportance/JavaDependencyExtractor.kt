package org.jetbrains.research.ictl.fileimportance

import com.intellij.openapi.project.Project
import com.intellij.psi.*

class JavaDependencyExtractor(project: Project) : IDependencyExtractor(project) {
    override fun extractEdges(): Sequence<DependencyEdge> {
        return getAllFiles()
            .flatMap {
                val result = mutableListOf<PsiElement>()
                it.acceptChildren(object : JavaRecursiveElementVisitor() {
                    override fun visitElement(element: PsiElement) {
                        if (
                            isClass(element)
                            || isFiled(element)
                            || isMethodAndNotConstructor(element)
                        ) {
                            result.add(element)
                        }

                        super.visitElement(element)
                    }
                })
                result
            }.buildDependencyEdges()
    }

    override fun getAllFiles(): Sequence<PsiFile> = getPsiFiles(project, "java")

    private fun isMethodAndNotConstructor(element: PsiElement) =
        PsiMethod::class.java.isAssignableFrom(element::class.java) && !(element as PsiMethod).isConstructor

    private fun isFiled(element: PsiElement) =
        PsiField::class.java.isAssignableFrom(element::class.java)

    private fun isClass(element: PsiElement) =
        PsiClass::class.java.isAssignableFrom(element::class.java)
}