package dev.yarlson.yar.external

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import dev.yarlson.yar.psi.YarFile
import java.io.File
import java.util.concurrent.TimeUnit

class YarExternalAnnotator : ExternalAnnotator<YarExternalAnnotator.Info, List<YarExternalAnnotator.Issue>>() {

    data class Info(val filePath: String, val document: Document, val packageName: String? = null)

    data class Issue(
        val file: String,
        val line: Int,
        val column: Int,
        val message: String,
    )

    override fun collectInformation(file: PsiFile): Info? {
        if (file !is YarFile) return null
        val virtualFile = file.virtualFile ?: return null
        val document = FileDocumentManager.getInstance().getDocument(virtualFile) ?: return null
        val path = virtualFile.path
        // Only annotate files on disk
        if (!File(path).exists()) return null
        // Detect package name to filter diagnostics for library packages
        val packageName = file.children
            .filterIsInstance<dev.yarlson.yar.psi.YarPackageDecl>()
            .firstOrNull()
            ?.node?.findChildByType(dev.yarlson.yar.psi.YarTypes.IDENTIFIER)?.text
        return Info(path, document, packageName)
    }

    override fun doAnnotate(collectedInfo: Info): List<Issue> {
        val yarPath = findYarExecutable() ?: return emptyList()
        val issues = runYarCheck(yarPath, collectedInfo.filePath)
        // Library packages (non-main) produce a spurious "package must be main"
        // diagnostic from `yar check`. Filter it out.
        if (collectedInfo.packageName != null && collectedInfo.packageName != "main") {
            return issues.filter { it.message != "package must be main" }
        }
        return issues
    }

    override fun apply(file: PsiFile, issues: List<Issue>, holder: AnnotationHolder) {
        val document = FileDocumentManager.getInstance().getDocument(file.virtualFile ?: return) ?: return
        val fileName = file.virtualFile?.name ?: return

        for (issue in issues) {
            // Match by filename (errors may use relative or absolute paths)
            if (!issue.file.endsWith(fileName) && issue.file != fileName) continue

            val line = issue.line - 1 // Convert to 0-based
            if (line < 0 || line >= document.lineCount) continue

            val lineStartOffset = document.getLineStartOffset(line)
            val lineEndOffset = document.getLineEndOffset(line)

            // Try to use column for precise range, fall back to whole line
            val col = issue.column - 1 // Convert to 0-based
            val startOffset = if (col >= 0) {
                (lineStartOffset + col).coerceAtMost(lineEndOffset)
            } else {
                lineStartOffset
            }

            // Highlight from column to end of word or end of line
            val text = document.getText(TextRange(startOffset, lineEndOffset))
            val wordEnd = text.indexOfFirst { it.isWhitespace() || it in "(){}[],:;" }
            val endOffset = if (wordEnd > 0) startOffset + wordEnd else lineEndOffset

            val range = TextRange(startOffset, endOffset.coerceAtLeast(startOffset + 1).coerceAtMost(lineEndOffset))

            holder.newAnnotation(HighlightSeverity.ERROR, issue.message)
                .range(range)
                .create()
        }
    }

    private fun runYarCheck(yarPath: String, filePath: String): List<Issue> {
        // Find the project root (directory containing the package)
        val file = File(filePath)
        val projectDir = file.parentFile ?: return emptyList()

        return try {
            val process = ProcessBuilder(yarPath, "check", projectDir.absolutePath)
                .directory(projectDir)
                .redirectErrorStream(true)
                .start()

            val completed = process.waitFor(10, TimeUnit.SECONDS)
            if (!completed) {
                process.destroyForcibly()
                return emptyList()
            }

            val output = process.inputStream.bufferedReader().readText()
            parseErrors(output)
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        private val ERROR_PATTERN = Regex("""^(.+?):(\d+):(\d+): (.+)$""")

        fun parseErrors(output: String): List<Issue> {
            return output.lines().mapNotNull { line ->
                val match = ERROR_PATTERN.matchEntire(line.trim()) ?: return@mapNotNull null
                val (file, lineNum, col, message) = match.destructured
                Issue(
                    file = file,
                    line = lineNum.toIntOrNull() ?: return@mapNotNull null,
                    column = col.toIntOrNull() ?: return@mapNotNull null,
                    message = message,
                )
            }
        }

        fun findYarExecutable(): String? {
            val pathExecutable = findYarExecutableOnPath()
            val fallbackCandidates = listOf(
                "/opt/homebrew/bin/yar",
                "/usr/local/bin/yar",
                "${System.getProperty("user.home")}/go/bin/yar",
                "${System.getProperty("user.home")}/.local/bin/yar",
            )
            return selectExecutable(
                listOf(System.getenv("YAR_PATH"), pathExecutable) + fallbackCandidates
            )
        }

        internal fun selectExecutable(candidates: List<String?>): String? {
            for (candidate in candidates) {
                if (candidate != null && File(candidate).canExecute()) {
                    return candidate
                }
            }
            return null
        }

        private fun findYarExecutableOnPath(): String? {
            return try {
                val process = ProcessBuilder("which", "yar")
                    .redirectErrorStream(true)
                    .start()
                val result = process.inputStream.bufferedReader().readText().trim()
                process.waitFor(5, TimeUnit.SECONDS)
                if (File(result).canExecute()) result else null
            } catch (_: Exception) {
                null
            }
        }
    }
}
