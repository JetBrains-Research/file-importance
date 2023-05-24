package org.jetbrains.research.ictl.fileimportance

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.startup.StartupManager
import com.intellij.psi.impl.source.tree.LightTreeUtil
import com.intellij.testFramework.useProject
import com.intellij.util.indexing.diagnostic.IndexDiagnosticDumperUtils
import com.intellij.util.indexing.impl.IndexStorageUtil
import com.intellij.util.io.exists
import com.intellij.warmup.util.OpenProjectArgs
import java.util.*
import kotlin.io.path.Path
import kotlin.system.exitProcess
import com.intellij.warmup.util.importOrOpenProject
import com.intellij.warmup.util.withLoggingProgresses
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import kotlin.io.path.pathString

class ImportProjectRunner : ApplicationStarter {
    @Deprecated("Specify it as `id` for extension definition in a plugin descriptor")
    override val commandName: String
        get() = "importProject"

    override fun main(args: List<String>) {
        log(Utils.BANNER)

        val projectPath = Path(args[1])
        if (!projectPath.toFile().exists()) {
            log("Path $projectPath does not exist")
            exitProcess(1)
        }


        val project = ProjectUtil.openOrImport(projectPath)
        if (project == null) {
            log("Could not open the project $projectPath")
            exitProcess(1)
        }


        ProjectManager.getInstance().addProjectManagerListener(project, object : ProjectManagerListener {
            override fun projectClosed(project: Project) {
                super.projectClosed(project)

                log("Close application")
                ApplicationManager.getApplication().invokeLater {
                    exitProcess(0)
                }
            }
        })

        log("Indexing project ${project.name}")
        val dumbService = exitOnNull(project.getService(DumbService::class.java)) {
            "Could not get DumbService"
        }

        dumbService.runWhenSmart {
            log("Indexing is finished")

//            ApplicationManager.getApplication().runReadAction {
//                log("Closing the project")
//
//                Timer().schedule(object: TimerTask(){
//                    override fun run() {
//                        ApplicationManager.getApplication().invokeLaterOnWriteThread(){
//                            ProjectManager.getInstance().closeAndDispose(project)
//
//                        }
//                    }
//                }, 300000)
//            }
        }
    }
}
