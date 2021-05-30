package dev.meanmail.typing

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VfsUtil.processFileRecursivelyWithoutIgnored
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiComment
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.startOffset
import com.jetbrains.extensions.python.toPsi
import com.jetbrains.python.PythonLanguage

data class Ignore(
    val file: VirtualFile,
    val range: TextRange,
    val code: String
) {
    override fun toString(): String {
        return code
    }
}

val pattern = Regex(".*(type: ?ignore(\\[[\\S-]+(,\\s*[\\S-]+)*])?).*")

fun getIgnore(project: Project): Map<VirtualFile, List<Ignore>> {
    val ignores = mutableMapOf<VirtualFile, MutableList<Ignore>>()
    val contentRoots = ProjectRootManager.getInstance(project).contentRoots
    val python = PythonLanguage.getInstance()

    for (root in contentRoots) {
        processFileRecursivelyWithoutIgnored(root) {
            if (it.isDirectory) {
                return@processFileRecursivelyWithoutIgnored true
            }
            val psi = it.toPsi(project) ?: return@processFileRecursivelyWithoutIgnored false
            if (psi.language != python) return@processFileRecursivelyWithoutIgnored true

            PsiTreeUtil.processElements(psi) { element ->
                if (element is PsiComment) {
                    val matches = pattern.findAll(element.text)
                    for (ignore in matches) {
                        val match = ignore.groups[1] ?: continue
                        val range = match.range
                        val start = element.startOffset + range.first
                        val endInclusive = element.startOffset + range.last
                        ignores.getOrPut(it) {
                            mutableListOf()
                        }.add(
                            Ignore(
                                it,
                                TextRange(start, endInclusive + 1),
                                match.value
                            )
                        )
                    }
                }
                return@processElements true
            }
            return@processFileRecursivelyWithoutIgnored true
        }
    }

    return ignores
}
