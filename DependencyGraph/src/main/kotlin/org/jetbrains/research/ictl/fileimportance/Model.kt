package org.jetbrains.research.ictl.fileimportance

import com.intellij.psi.PsiElement

data class DependencyEdge(
    val sourceElement: PsiElement,
    val destinationElement: PsiElement,
)

data class FileInformation(
    val elementName: String,
    val fileName: String
)

data class JsonDependencyEdge(
    val source: String,
    val destination: String
)

enum class DependencyType{
    METHOD, CLASS
}