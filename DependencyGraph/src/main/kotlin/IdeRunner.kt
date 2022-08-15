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

    override fun getCommandName(): String = "mine-dependencies"

    private val gson = Gson()

    override fun main(args: Array<String>) {
        log(
            "    ____                            __                         __  ____                \n" +
                    "   / __ \\___  ____  ___  ____  ____/ /__  ____  _______  __   /  |/  (_)___  ___  _____\n" +
                    "  / / / / _ \\/ __ \\/ _ \\/ __ \\/ __  / _ \\/ __ \\/ ___/ / / /  / /|_/ / / __ \\/ _ \\/ ___/\n" +
                    " / /_/ /  __/ /_/ /  __/ / / / /_/ /  __/ / / / /__/ /_/ /  / /  / / / / / /  __/ /    \n" +
                    "/_____/\\___/ .___/\\___/_/ /_/\\__,_/\\___/_/ /_/\\___/\\__, /  /_/  /_/_/_/ /_/\\___/_/     \n" +
                    "          /_/                                     /____/                               "
        )

        val depType = getDependencyType(args[1]) ?: run {
            log("Can not find dependency type $args[1]")
            exitProcess(1)
        }

        val path = args[2]
        val graphPath = args[3]
        val informationPath = args[4]

        val psiFiles = getAllRelatedFiles(path)
        val elements = getAllRelatedElements(psiFiles, depType)
        val edges = buildDependencyGraph(elements)


        exportGraphToJson(edges, graphPath)
        exportClasses(elements, informationPath)

        exitProcess(0)
    }

    private fun exportClasses(elements: List<PsiElement>, informationPath: String) {
        log("Exporting class informaiton")
        if (elements.isEmpty()) {
            return
        }


        val classes = if (elements[0] is PsiClass) {
            elements
                .map { e -> e as PsiClass }
        } else if (elements[0] is PsiMethod) {
            elements
                .map { e -> e as PsiMethod }
                .mapNotNull { pm -> pm.containingClass }
        } else {
            log("Unrecognized PsiElement")
            return;
        }

        val result = classes
            .map { pc ->
                FileInformation(
                    pc.qualifiedName ?: "Some Local/Anonymous class",
                    pc.containingFile.virtualFile.presentableName
                )
            }

        writeToJson(result, informationPath)
    }

    private fun writeToJson(data: List<FileInformation>, path: String) {
        try {
            val jsonString = gson.toJson(data)
            File(path).printWriter().use {
                it.write(jsonString)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun exportGraphToJson(edges: List<DependencyEdge>, path: String) {
        log("exporting graph to $path")

        val jsonEdges = edges.map { e ->
            JsonDependencyEdge(
                e.sourceElement.containingFile.virtualFile.presentableName,
                e.destinationElement.containingFile.virtualFile.presentableName
            )
        }

        try {
            val jsonString = gson.toJson(jsonEdges)
            File(path).printWriter().use {
                it.write(jsonString)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildDependencyGraph(elements: List<PsiElement>): MutableList<DependencyEdge> {
        log("Building graph")
        val edges = mutableListOf<DependencyEdge>()

        elements.forEach { c ->
            val newEdges = ReferencesSearch
                .search(c)
                .map { r -> DependencyEdge(r.element, c) }
            edges.addAll(newEdges)
        }
        return edges
    }

    private fun getAllRelatedElements(psiFiles: List<PsiFile?>, depType: DependencyType): List<PsiElement> {

        val clazz = if (depType == DependencyType.CLASS) {
            PsiClass::class.java
        } else {
            PsiMethod::class.java
        }

        val elements = getAllPsiElements(psiFiles, clazz)
        return elements
    }

    private fun getAllRelatedFiles(path: String): List<PsiFile?> {
        log("Getting All related files")

        val project = ProjectUtil.openOrImport(Path(path))
        val files = FilenameIndex.getAllFilesByExt(project, "java")
        val psiManager = PsiManager.getInstance(project)
        return files.mapNotNull { f -> psiManager.findFile(f) }
    }

    private fun <T> getAllPsiElements(psiFiles: List<PsiFile?>, clazz: Class<T>): List<PsiElement> {
        log("Getting All related elements")
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

    private fun log(log: String) {
        println("****Miner**** $log")
    }

    private fun getDependencyType(name: String): DependencyType? {
        return try {
            DependencyType.valueOf(name)
        } catch (e: Exception) {
            null
        }
    }
}
