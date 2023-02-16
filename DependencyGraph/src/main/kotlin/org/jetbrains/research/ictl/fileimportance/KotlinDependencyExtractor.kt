package org.jetbrains.research.ictl.fileimportance

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.refactoring.suggested.startOffset

class KotlinDependencyExtractor(project: Project) : DependencyExtractor(project) {
    override fun extractEdges(): Sequence<DependencyEdge> {
        val psiManager = PsiManager.getInstance(project)
        return getAllFiles()
            .flatMap {
                val result = mutableListOf<DependencyEdge>()
                val foundElementOffsets = mutableListOf<Int>()

                try {
                    it.acceptChildren(
                        JavaRecursiveElementVisitor(it.getFileName()) { element ->
                            if (!foundElementOffsets.contains(element.startOffset)) {
                                element.references
                                    .filterNotNull()
                                    .forEach {
                                        val resolve = it.resolve()
                                        if (resolve != null && psiManager.isInProject(resolve) && resolve.containingFile != null &&
                                            !it.element.containingFile.isEquivalentTo(resolve.containingFile)
                                        ) {
                                            result.add(
                                                DependencyEdge(
                                                    filename,
                                                    resolve.getFileName()
                                                )
                                            )
                                        }
                                    }
                            }
                        }
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                result
            }
    }

    override fun getAllFiles(): Sequence<PsiFile> = getPsiFiles(project, "kt")
    override fun prepare() = getAllFiles().forEach { it.acceptChildren(object : JavaRecursiveElementVisitor() {}) }
}
