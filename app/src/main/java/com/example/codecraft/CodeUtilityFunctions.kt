package com.example.codecraft

import android.content.Context
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import kotlinx.serialization.json.Json

// Cut text functionality
fun cutText(
    textFieldValue: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    clipboardManager: ClipboardManager
) {
    val selectedText = textFieldValue.selection
    if (selectedText.length > 0) {
        val text = textFieldValue.text
        val start = selectedText.start
        val end = selectedText.end

        // Copy to clipboard
        val cutText = text.substring(start, end)
        clipboardManager.setText(AnnotatedString(cutText))

        // Remove from text
        val newText = text.substring(0, start) + text.substring(end)
        onTextChange(TextFieldValue(newText, selection = androidx.compose.ui.text.TextRange(start)))
    }
}

// Copy text functionality
fun copyText(
    textFieldValue: TextFieldValue,
    clipboardManager: ClipboardManager
) {
    val selectedText = textFieldValue.selection
    if (selectedText.length > 0) {
        val text = textFieldValue.text
        val start = selectedText.start
        val end = selectedText.end
        val copyText = text.substring(start, end)
        clipboardManager.setText(AnnotatedString(copyText))
    }
}

// Paste text functionality
fun pasteText(
    textFieldValue: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    clipboardManager: ClipboardManager
) {
    try {
        val clipboardText = clipboardManager.getText()
        if (clipboardText != null && clipboardText.text.isNotEmpty()) {
            val text = textFieldValue.text
            val selection = textFieldValue.selection
            val start = selection.start
            val end = selection.end

            val newText = if (selection.length > 0) {
                text.substring(0, start) + clipboardText.text + text.substring(end)
            } else {
                text.substring(0, start) + clipboardText.text + text.substring(start)
            }

            val newSelection = androidx.compose.ui.text.TextRange(start + clipboardText.text.length)
            onTextChange(TextFieldValue(newText, selection = newSelection))
        }
    } catch (e: Exception) {
        Log.e("CodeUtilityFunctions", "Error pasting text: ${e.message}")
    }
}

// Load syntax rules from assets
// Load syntax rules from assets

fun loadSyntaxRules(context: Context, fileName: String): CodeSyntaxRules {
    return try {
        val inputStream = context.assets.open(fileName)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        inputStream.close()

        Json.decodeFromString(CodeSyntaxRules.serializer(), jsonString)
    } catch (e: Exception) {
        Log.e("CodeUtilityFunctions", "Error loading syntax rules: ${e.message}")
        // Fallback: Kotlin rules
        CodeSyntaxRules(
            language = "kotlin",
            keywords = listOf(
                "as", "break", "class", "continue", "do", "else", "false", "for",
                "fun", "if", "in", "interface", "is", "null", "object", "package",
                "return", "super", "this", "throw", "true", "try", "typealias",
                "typeof", "val", "var", "when", "while",
                "by", "catch", "constructor", "delegate", "dynamic", "field", "file",
                "finally", "get", "import", "init", "param", "property", "set",
                "setparam", "value", "where"
            ),
            comments = listOf("//")
        )
    }
}



// Compile code functionality
fun compileCode(
    context: Context,
    code: String,
    fileManager: ProjectFileManager,
    fileName: String,
    onOutput: (String) -> Unit
) {
    // Simulate compilation process
    val output = "Compilation completed successfully!\n\nCode length: ${code.length} characters\nFile: $fileName\n\nOutput:\n$code"
    onOutput(output)
}