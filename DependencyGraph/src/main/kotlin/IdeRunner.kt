import com.google.gson.Gson
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.psi.*
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.searches.ReferencesSearch
import java.io.File
import kotlin.io.path.Path
import kotlin.system.exitProcess

class IdeRunner : ApplicationStarter {

    val graphPath = "/Users/psycho/Desktop/Jetbrains/shared/graph.json"
    val informationPath = "/Users/psycho/Desktop/Jetbrains/shared/info.json"


    override fun getCommandName(): String = "mine-dependencies"

    override fun main(args: Array<String>) {
        println("Hello World!")

        val path = "/Users/psycho/Desktop/Jetbrains/RxJava-3.x"
//        val path = "/Users/psycho/Desktop/Jetbrains/simple-springboot-app"
//        val path = "/Users/psycho/Desktop/Jetbrains/logging-log4j2"
        val psiFiles = getAllRelatedFiles(path)
        val elements = getAllRelatedElements(psiFiles)
        val edges = buildDependencyGraph(elements)


        println("********************")
//        nodes.forEach { println(it) }
        exportGraphToJson(edges)
        exportClasses(elements)


        exitProcess(0)
    }

    private fun exportClasses(elements: List<PsiElement>) {
        val result = elements
            .mapNotNull{ e -> e as PsiClass }
            .map { pc -> FileInformation(pc.qualifiedName!!, pc.containingFile.virtualFile.presentableName) }

        writeToJson(result, informationPath)
    }

    private fun writeToJson(data: List<FileInformation>, path: String) {
        try {
            val gson = Gson()
            val jsonString = gson.toJson(data)
            File(path).printWriter().use {
                it.write(jsonString)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun exportGraphToJson(edges: List<DependencyEdge>) {
        val jsonEdges = edges.map{ e -> JsonDependencyEdge(
            e.sourceElement.containingFile.virtualFile.presentableName,
            e.destinationElement.containingFile.virtualFile.presentableName
        )}

        try {
            val gson = Gson()
            val jsonString = gson.toJson(jsonEdges)
            File(graphPath).printWriter().use {
                it.write(jsonString)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildDependencyGraph(elements: List<PsiElement>): MutableList<DependencyEdge> {
        println("Building graph")
        val edges = mutableListOf<DependencyEdge>()

        elements.forEach { c ->
            val newEdges = ReferencesSearch
                .search(c)
                .map { r -> DependencyEdge(r.element, c)}
            edges.addAll(newEdges)
        }
        return edges
    }

    private fun getAllRelatedElements(psiFiles: List<PsiFile?>): List<PsiElement> {
        val elements = getAllPsiElements(psiFiles, PsiClass::class.java)
        return elements
    }

    private fun getAllRelatedFiles(path: String): List<PsiFile?> {
        println("Getting All related files")

        val project = ProjectUtil.openOrImport(Path(path))
        val files = FilenameIndex.getAllFilesByExt(project, "java")
        val psiManager = PsiManager.getInstance(project)
        return files.mapNotNull { f -> psiManager.findFile(f) }
    }

    private fun <T> getAllPsiElements(psiFiles: List<PsiFile?>, clazz: Class<T>): List<PsiElement> {
        println("Getting All related elements")
        val elements = mutableListOf<PsiElement>()
        psiFiles.forEach {
            it?.acceptChildren(object : JavaRecursiveElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (clazz.isAssignableFrom(element::class.java)) {
                        elements.add(element)
                    }

                    super.visitElement(element)
                }
            })
        }
        return elements
    }
}
