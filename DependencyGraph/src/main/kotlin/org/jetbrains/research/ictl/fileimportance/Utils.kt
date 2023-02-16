package org.jetbrains.research.ictl.fileimportance

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import kotlin.system.exitProcess

object Utils {
    const val BANNER =
        "\n    ____                            __                         __  ____                \n" +
                "   / __ \\___  ____  ___  ____  ____/ /__  ____  _______  __   /  |/  (_)___  ___  _____\n" +
                "  / / / / _ \\/ __ \\/ _ \\/ __ \\/ __  / _ \\/ __ \\/ ___/ / / /  / /|_/ / / __ \\/ _ \\/ ___/\n" +
                " / /_/ /  __/ /_/ /  __/ / / / /_/ /  __/ / / / /__/ /_/ /  / /  / / / / / /  __/ /    \n" +
                "/_____/\\___/ .___/\\___/_/ /_/\\__,_/\\___/_/ /_/\\___/\\__, /  /_/  /_/_/_/ /_/\\___/_/     \n" +
                "          /_/                                     /____/                               "
}

inline fun <reified T> log(log: T) {
    println("****Miner**** $log")
}

fun getPsiFiles(project: Project, ext: String) = sequence {
    log("get all $ext files")
    val psiManager = PsiManager.getInstance(project)

    FilenameIndex.getAllFilesByExt(project, ext, GlobalSearchScope.projectScope(project)).forEach {
        val file = psiManager.findFile(it)
        // yieldNotNull raises java.lang.ClassNotFoundException: org.jetbrains.kotlin.utils.CollectionsKt
        file?.let {
            yield(file)
        }
    }
}

/**
 * Almost the same as [require] from STD
 */
internal inline fun exitOnFalse(value: Boolean, lazyErrorMessage: () -> String) {
    if (!value) exitWithMessage(lazyErrorMessage)
}

internal inline fun <T> exitOnNull(value: T?, lazyErrorMessage: () -> String): T {
    if (value == null) exitWithMessage(lazyErrorMessage)
    return value
}

internal inline fun exitWithMessage(lazyErrorMessage: () -> String): Nothing {
    log(lazyErrorMessage())
    exitProcess(1)
}
