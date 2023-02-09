package org.jetbrains.research.ictl.fileimportance

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.gist.PsiFileGist
import org.jetbrains.kotlin.idea.structuralsearch.visitor.KotlinRecursiveElementVisitor
import org.jetbrains.kotlin.psi.*

class KotlinDependencyExtractor(project: Project) : IDependencyExtractor(project) {
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
//                                    log(it.element.text)
                                        if (resolve != null && psiManager.isInProject(resolve) && resolve.containingFile != null &&
                                            !it.element.containingFile.isEquivalentTo(resolve.containingFile)
                                        ) {
                                            result.add(DependencyEdge(it.element.getFileName(), resolve.getFileName()))
                                        }
                                    }
                            }

                            super.visitElement(element)
                        }
                    })
                }catch (e:Exception){
                    e.printStackTrace()
                }
                result
            }
    }

    override fun getAllFiles(): Sequence<PsiFile> = getPsiFiles(project, "kt")
    override fun prepare() = getAllFiles().forEach {it.acceptChildren(object: JavaRecursiveElementVisitor(){}) }

    private fun isProperty(element: KtElement) =
        KtProperty::class.java.isAssignableFrom(element::class.java)

    private fun isMethodAndNotConstructor(element: KtElement) =
        KtFunction::class.java.isAssignableFrom(element::class.java) && PsiFile::class.java.isAssignableFrom(element.parent::class.java)

    private fun isClass(element: KtElement) = KtClass::class.java.isAssignableFrom(element::class.java)

}