import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

public data class DependencyEdge(
    val sourceElement: PsiElement,
    val destinationElement: PsiElement,
)