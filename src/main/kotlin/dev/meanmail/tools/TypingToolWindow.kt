package dev.meanmail.tools

import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import com.intellij.ui.treeStructure.Tree
import dev.meanmail.typing.Ignore
import dev.meanmail.typing.getIgnore
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JComponent
import javax.swing.event.TreeModelListener
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath


class TypingToolWindow(private val project: Project) : MouseListener {
    private fun getTreeModel(): TreeModel {
        return IgnoreTreeModel(project)
    }

    private val typeIgnoreList: DialogPanel
        get() {
            val ignoreList = Tree(getTreeModel())
            ignoreList.addMouseListener(this)
            val scroll = JBScrollPane(ignoreList)

            return panel {
                row {
                    scroll(CCFlags.grow)
                }
            }
        }

    val content: JComponent
        get() {
            return typeIgnoreList
        }

    override fun mouseClicked(e: MouseEvent?) {
        if (e?.clickCount != 2) {
            return
        }
        val tree = (e.component as? Tree) ?: return
        val ignore = (tree.selectionModel
            .selectionPath.lastPathComponent as? Ignore) ?: return
        val textEditor = (FileEditorManager.getInstance(project)
            .openFile(ignore.file, true)
            .first() as? TextEditor)?.editor ?: return
        textEditor.caretModel.moveToOffset(
            ignore.range.startOffset
        )
        textEditor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
    }

    override fun mousePressed(e: MouseEvent?) {
    }

    override fun mouseReleased(e: MouseEvent?) {
    }

    override fun mouseEntered(e: MouseEvent?) {
    }

    override fun mouseExited(e: MouseEvent?) {
    }
}


class IgnoreTreeModel(project: Project) : TreeModel {
    private val ignores = getIgnore(project)
    private val root = "Type ignore"

    override fun getRoot(): Any {
        return root
    }

    private fun getChildren(parent: Any?): List<Any> {
        return if (parent == root) {
            ignores.keys.toList()
        } else {
            ignores[parent] ?: return emptyList()
        }
    }

    override fun getChild(parent: Any?, index: Int): Any {
        return getChildren(parent)[index]
    }

    override fun getChildCount(parent: Any?): Int {
        return getChildren(parent).size
    }

    override fun isLeaf(node: Any?): Boolean {
        return node != root && node !in ignores.keys
    }

    override fun valueForPathChanged(path: TreePath?, newValue: Any?) {
    }

    override fun getIndexOfChild(parent: Any?, child: Any?): Int {
        return getChildren(parent).indexOf(child)
    }

    override fun addTreeModelListener(l: TreeModelListener?) {
    }

    override fun removeTreeModelListener(l: TreeModelListener?) {
    }

}
