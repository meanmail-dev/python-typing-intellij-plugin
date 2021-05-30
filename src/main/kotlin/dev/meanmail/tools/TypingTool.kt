package dev.meanmail.tools

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory


class TypingTool : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val typingToolWindow = TypingToolWindow(project)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val ignoreList = contentFactory.createContent(
            typingToolWindow.content, "Ignore", false
        )
        toolWindow.contentManager.addContent(ignoreList)
    }
}
