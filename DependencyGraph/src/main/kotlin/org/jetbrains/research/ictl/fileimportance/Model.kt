package org.jetbrains.research.ictl.fileimportance

import kotlinx.serialization.Serializable

@Serializable
data class DependencyEdge(
    val source: String,
    val destination: String
)
@Serializable
data class Features(
    val path: String,
    val equal: Double
)

enum class DependencyType {
    METHOD, CLASS
}