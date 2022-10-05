package org.jetbrains.research.ictl.fileimportance

import kotlinx.serialization.Serializable

@Serializable
data class DependencyEdge(
    val source: String,
    val destination: String
)

enum class DependencyType {
    METHOD, CLASS
}