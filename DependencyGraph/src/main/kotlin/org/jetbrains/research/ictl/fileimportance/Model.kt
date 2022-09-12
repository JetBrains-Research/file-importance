package org.jetbrains.research.ictl.fileimportance

import com.intellij.psi.PsiElement
import kotlinx.serialization.Serializable

@Serializable
data class FileInformation(
    val elementName: String,
    val fileName: String
)

@Serializable
data class DependencyEdge(
    val source: String,
    val destination: String
)

enum class DependencyType {
    METHOD, CLASS
}