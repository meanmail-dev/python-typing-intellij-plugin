package dev.meanmail.tools

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.ui.layout.panel
import com.intellij.ui.treeStructure.Tree
import dev.meanmail.typing.Ignore
import dev.meanmail.typing.getIgnore
import java.awt.event.ActionEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JComponent
import javax.swing.event.TreeModelListener
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath


class TypingToolWindow(private val project: Project) : MouseListener, BulkFileListener {
    private val ignores: Map<VirtualFile, List<Ignore>>
        get() = getIgnore(project)

    private fun getTreeModel(): TreeModel {
        return IgnoreTreeModel(ignores)
    }

    private val ignoreTree: Tree by lazy {
        val tree = Tree(getTreeModel())
        tree.addMouseListener(this)

        tree
    }

    private val typeIgnoreList: DialogPanel
        get() {
            return panel {
                row {
                    panel {
                        button("Update", ::updateTree)
                    }
                }
                row {
                    scrollPane(ignoreTree)
                }
            }
        }

    private fun updateTree(event: ActionEvent) {
        val application = ApplicationManager.getApplication()
        application.executeOnPooledThread {
            val selection = ignoreTree.selectionPath
            application.invokeAndWait {
                ignoreTree.setPaintBusy(true)
            }
            var model = ignoreTree.model
            application.runReadAction {
                model = getTreeModel()
            }
            application.invokeLater {
                ignoreTree.model = model
                ignoreTree.setPaintBusy(false)
                if (ignoreTree.selectionPath == null) {
                    ignoreTree.selectionPath = selection
                }
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


class IgnoreTreeModel(private val ignores: Map<VirtualFile, List<Ignore>>) : TreeModel {
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
