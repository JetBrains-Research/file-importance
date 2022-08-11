import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

public data class DependencyEdge(
    val sourceElement: PsiElement,
    val destinationElement: PsiElement,
)

public data class FileInformation(
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