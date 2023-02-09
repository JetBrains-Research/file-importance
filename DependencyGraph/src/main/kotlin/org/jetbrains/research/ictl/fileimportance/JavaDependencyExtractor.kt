package org.jetbrains.research.ictl.fileimportance

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.refactoring.suggested.startOffset

class JavaDependencyExtractor(project: Project) : IDependencyExtractor(project) {
    override fun extractEdges(): Sequence<DependencyEdge> {
        val psiManager = PsiManager.getInstance(project)
        return getAllFiles()
            .flatMap {
                val result = mutableListOf<DependencyEdge>()
                val foundElementOffsets = mutableListOf<Int>()

//                log("File -> ${it.getFileName()}")

                try{
                    it.acceptChildren(object : JavaRecursiveElementVisitor() {
                        override fun visitElement(element: PsiElement) {
                            if (!foundElementOffsets.contains(element.startOffset)) {
//                            foundElementOffsets.add(element.startOffset)

                                element.references
                                    .filterNotNull()
                                    .forEach {
                                        val resolve = it.resolve()
                                        if (resolve != null && psiManager.isInProject(resolve) && resolve.containingFile != null &&
                                            !element.containingFile.isEquivalentTo(resolve.containingFile)
                                        ) {
                                            result.add(DependencyEdge(element.getFileName(), resolve.getFileName()))
                                        }
                                    }
                            }

                            super.visitElement(element)
                        }
                    })
                }catch (e: Exception){
                    e.printStackTrace()
                }
                result
            }
    }

    override fun getAllFiles(): Sequence<PsiFile> = getPsiFiles(project, "java")
    override fun prepare() = getAllFiles().forEach { it.acceptChildren(object : JavaRecursiveElementVisitor() {}) }

    private fun isMethodAndNotConstructor(element: PsiElement) =
        PsiMethod::class.java.isAssignableFrom(element::class.java) && !(element as PsiMethod).isConstructor

    private fun isFiled(element: PsiElement) =
        PsiField::class.java.isAssignableFrom(element::class.java)

    private fun isClass(element: PsiElement) =
        PsiClass::class.java.isAssignableFrom(element::class.java)
}