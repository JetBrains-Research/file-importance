package org.jetbrains.research.ictl.fileimportance

import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiElement

abstract class JavaRecursiveElementVisitorForFile : JavaRecursiveElementVisitor() {
    abstract val filename: String
}

fun JavaRecursiveElementVisitor(
    filename: String,
    overrideVisitElement: JavaRecursiveElementVisitorForFile.(PsiElement) -> Unit
) =
    object : JavaRecursiveElementVisitorForFile() {
        override val filename = filename

        override fun visitElement(element: PsiElement) {
            this.overrideVisitElement(element)
            super.visitElement(element)
        }
    }