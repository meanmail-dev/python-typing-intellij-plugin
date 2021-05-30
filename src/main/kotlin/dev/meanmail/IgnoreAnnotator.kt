package dev.meanmail


import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.startOffset
import dev.meanmail.typing.pattern

class IgnoreAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is PsiComment) {
            return
        }
        val ignores = pattern.findAll(element.text)
        for (ignore in ignores) {
            val range = ignore.groups[1]?.range
            val startOffset = element.startOffset + (range?.start ?: 0)
            val endOffset = element.startOffset + (range?.endInclusive ?: 0)
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(TextRange(startOffset, endOffset + 1))
                .textAttributes(
                    TextAttributesKey.createTextAttributesKey(
                        "TYPING.IGNORE",
                        DefaultLanguageHighlighterColors.STRING
                    )
                )
                .create()
        }
    }
}
