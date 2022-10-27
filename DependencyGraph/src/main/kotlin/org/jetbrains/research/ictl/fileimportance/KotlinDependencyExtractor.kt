package org.jetbrains.research.ictl.fileimportance

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.idea.structuralsearch.visitor.KotlinRecursiveElementVisitor
import org.jetbrains.kotlin.psi.*

class KotlinDependencyExtractor(project: Project) : IDependencyExtractor(project) {
    override fun extractEdges(): Sequence<DependencyEdge> {

        return getAllFiles()
            .flatMap {
                val result = mutableListOf<PsiElement>()
                it.acceptChildren(object: KotlinRecursiveElementVisitor(){
                    override fun visitKtElement(element: KtElement) {
                        if (isClass(element)
                            || isMethodAndNotConstructor(element)
                            || isProperty(element)){
                            result.add(element)
                        }
                        super.visitKtElement(element)
                    }
                })
                result
            }
            .buildDependencyEdges()
    }

    override fun getAllFiles(): Sequence<PsiFile> = getPsiFiles(project, "kt")

    private fun isProperty(element: KtElement) =
        KtProperty::class.java.isAssignableFrom(element::class.java)

    private fun isMethodAndNotConstructor(element: KtElement) =
        KtFunction::class.java.isAssignableFrom(element::class.java) && !KtConstructor::class.java.isAssignableFrom(element::class.java)

    private fun isClass(element: KtElement) = KtClass::class.java.isAssignableFrom(element::class.java)

}