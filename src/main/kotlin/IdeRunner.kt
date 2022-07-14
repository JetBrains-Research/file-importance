import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.psi.*
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import kotlin.io.path.Path
import kotlin.system.exitProcess

class IdeRunner : ApplicationStarter {
    override fun getCommandName(): String = "mine-dependencies"

    override fun main(args: Array<String>) {
        println("Hello World!")

        // Try adding program arguments via Run/Debug configuration.
        // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
        println("Program arguments: ${args.joinToString()}")

//        val path = "/Users/psycho/Desktop/Jetbrains/simple-springboot-app"
        val path = "/Users/psycho/Desktop/Jetbrains/logging-log4j2"
        val project = ProjectUtil.openOrImport(Path(path))
        val files = FilenameIndex.getAllFilesByExt(project, "java")
        val psiManager = PsiManager.getInstance(project)
        val psiFiles = files.map { f -> psiManager.findFile(f) }

        var elements = GetAllPsiElements(psiFiles, PsiClass::class.java)

        elements = elements
            .filter { e -> e.name != null }
            .toMutableList()

        val edges = mutableSetOf<Pair<PsiElement, PsiElement>>()

        elements.forEach { c ->
            val newEdges = mutableSetOf<Pair<PsiElement, PsiElement>>()
            ReferencesSearch.search(c)
                .forEach { r ->
                    val callerClass = PsiTreeUtil.getParentOfType(r.element, PsiClass::class.java)
                    if(callerClass != null){
                        newEdges.add(
                            Pair(
                                callerClass,
                                c
                            )
                        )
                    }
                }

            edges.addAll(newEdges)
        }


        println("********************")
//        nodes.forEach { println(it) }
        edges.forEach { e ->
            val firstName = (e.first as PsiNameIdentifierOwner).nameIdentifier?.text    
            val secondName = (e.second as PsiNameIdentifierOwner).nameIdentifier?.text    
            println("($firstName->$secondName)") 
        }


        exitProcess(0)
    }

    private fun <T> GetAllPsiElements(psiFiles: List<PsiFile?>, clazz: Class<T>): MutableList<T> {
        var elements = mutableListOf<T>()
        psiFiles.forEach {
            it?.acceptChildren(object : JavaRecursiveElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (clazz.isAssignableFrom(element::class.java)) {
                        elements.add(element as T)
                    }

                    super.visitElement(element)
                }
            })
        }
        return elements
    }
}
