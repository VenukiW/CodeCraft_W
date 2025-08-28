package com.example.codecraft


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.codecraft.ui.theme.CodeCraftTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import com.example.codecraft.compileCodeToServer
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.Alignment


@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private lateinit var projectFileManager: ProjectFileManager
    private var currentFileName by mutableStateOf("Untitled")
    private val codeEditorState = CodeEditorState()
    private var syntaxRules by mutableStateOf(
        CodeSyntaxRules(keywords = listOf(), comments = listOf())
    )

    override fun onPause() {
        super.onPause()
        saveCurrentFile(currentFileName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val clipboardManager = LocalClipboardManager.current
            syntaxRules = when {
                currentFileName.endsWith(".py") -> loadSyntaxRules(this, "python.json")
                currentFileName.endsWith(".java") -> loadSyntaxRules(this, "java.json")
                currentFileName.endsWith(".kt") -> loadSyntaxRules(this, "kotlin.json")
                else -> loadSyntaxRules(this, "kotlin.json")
            }

            var showEditorToolbar by remember { mutableStateOf(false) }
            var showSearchReplace by remember { mutableStateOf(false) }
            var showCompilationInterface by remember { mutableStateOf(false) }
            var compilationOutput by remember { mutableStateOf("") }
            val scope = rememberCoroutineScope()
            val context = LocalContext.current

            // Auto-save and commit changes
            LaunchedEffect(codeEditorState.textField.value) {
                snapshotFlow { codeEditorState.textField.value }
                    .debounce(500)
                    .collect {
                        codeEditorState.commitChange()
                        saveCurrentFile(currentFileName)
                    }
            }

            projectFileManager = ProjectFileManager(context)

            CodeCraftTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        MainMenuBar(
                            currentFileName = currentFileName,
                            context = context,
                            onNewFile = { createNewFile(it) },
                            onOpenFile = { openExistingFile(it) },
                            onSaveFile = { saveCurrentFile(it) }
                        )
                    },


                    bottomBar = {
                        // Calculate word and character count
                        val editorTextValue = codeEditorState.textField.value.text
                        val characterCount = editorTextValue.length
                        val wordCount = remember(editorTextValue) {
                            editorTextValue.trim()
                                .split("\\s+".toRegex())
                                .filter { it.isNotEmpty() }
                                .size
                        }

                        Column {
                            // Word/Character counter above toolbar
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Words: $wordCount • Characters: $characterCount",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }

                            // Existing BottomAppBar
                            BottomAppBar(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                actions = {
                                    IconButton(onClick = { codeEditorState.undo() }) {
                                        Icon(
                                            imageVector = Icons.Default.Undo,
                                            contentDescription = "Undo",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                    IconButton(onClick = { codeEditorState.redo() }) {
                                        Icon(
                                            imageVector = Icons.Default.Redo,
                                            contentDescription = "Redo",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                    IconButton(onClick = { showSearchReplace = !showSearchReplace }) {
                                        Icon(
                                            imageVector = Icons.Default.FindReplace,
                                            contentDescription = "Search & Replace",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                    IconButton(onClick = {
                                        val code = codeEditorState.textField.value.text
                                        compileCodeToServer(code) { output ->
                                            compilationOutput = output
                                            showCompilationInterface = true
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "Compile & Run",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }

                                    IconButton(onClick = { showEditorToolbar = !showEditorToolbar }) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCut,
                                            contentDescription = "Editor Tools",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        // Compilation Interface
                        if (showCompilationInterface) {
                            CodeCompilationInterface(
                                clipboardManager = clipboardManager,
                                output = compilationOutput,
                                onClose = { showCompilationInterface = false }
                            )
                        }

                        // Search & Replace Bar
                        if (showSearchReplace) {
                            CodeSearchReplaceBar(
                                editorState = codeEditorState,
                                onClose = { showSearchReplace = false }
                            )
                        }

                        // Editor Toolbar
                        if (showEditorToolbar) {
                            CodeEditorToolbar(
                                onCut = {
                                    cutText(
                                        codeEditorState.textField.value,
                                        { codeEditorState.onTextChange(it) },
                                        clipboardManager
                                    )
                                },
                                onCopy = {
                                    copyText(
                                        codeEditorState.textField.value,
                                        clipboardManager
                                    )
                                },
                                onPaste = {
                                    pasteText(
                                        codeEditorState.textField.value,
                                        { codeEditorState.onTextChange(it) },
                                        clipboardManager
                                    )
                                }
                            )
                        }




                        // Main Code Editor
                        CodeCraftEditor(
                            modifier = Modifier.weight(1f),
                            editorState = codeEditorState,
                            syntaxRules = syntaxRules
                        )
                    }
                }
            }
        }
    }

    // Create a new file and clear the editor
    private fun createNewFile(filename: String) {
        val file = projectFileManager.createNewFile(filename)
        currentFileName = file
        codeEditorState.textField.value = TextFieldValue("")

        // Update syntax rules
        syntaxRules = when {
            filename.endsWith(".py") -> loadSyntaxRules(this, "python.json")
            filename.endsWith(".java") -> loadSyntaxRules(this, "java.json")
            filename.endsWith(".kt") -> loadSyntaxRules(this, "kotlin.json")
            else -> loadSyntaxRules(this, "kotlin.json")
        }
    }

    // Save current editor content to a file
    private fun saveCurrentFile(filename: String) {
        projectFileManager.saveFile(filename, codeEditorState.textField.value.text)
    }

    // Open a file and load its content into the editor
    private fun openExistingFile(filename: String) {
        val content = projectFileManager.openFile(filename)
        codeEditorState.textField.value = TextFieldValue(content)
        currentFileName = filename

        // Update syntax rules for new file
        syntaxRules = when {
            filename.endsWith(".py") -> loadSyntaxRules(this, "python.json")
            filename.endsWith(".java") -> loadSyntaxRules(this, "java.json")
            filename.endsWith(".kt") -> loadSyntaxRules(this, "kotlin.json")
            else -> loadSyntaxRules(this, "kotlin.json")
        }
    }
}

@Composable
fun CodeCraftEditor(
    modifier: Modifier,
    editorState: CodeEditorState,
    syntaxRules: CodeSyntaxRules
) {
    val editorText = editorState.textField.value
    val scrollState = rememberScrollState()
    val lines = editorText.text.split("\n").ifEmpty { listOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row {
            // Line numbers column
            Column(
                modifier = Modifier
                    .width(60.dp)
                    .padding(end = 8.dp)
            ) {
                lines.forEachIndexed { i, _ ->
                    Text(
                        text = "${i + 1}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .height(24.dp)
                            .padding(vertical = 2.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Main text editor
            BasicTextField(
                value = editorText,
                onValueChange = { editorState.onTextChange(it) },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color.Transparent,
                    lineHeight = 24.sp
                ),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                decorationBox = { innerTextField ->
                    Box {
                        androidx.compose.material3.Text(
                            text = highlightCodeSyntax(editorText.text, syntaxRules),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 24.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        if (editorText.text.isEmpty()) {
                            Text(
                                "Start coding here...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 24.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

// Enhanced syntax highlighting with modern colors
fun highlightCodeSyntax(text: String, rules: CodeSyntaxRules): AnnotatedString {
    val keywordColor = when (rules.language.lowercase()) {
        "python" -> Color(0xFF569CD6) // blue
        "java" -> Color(0xFFD73A49)   // red
        "kotlin" -> Color(0xFF9CDCFE) // light blue
        else -> Color(0xFF569CD6)
    }

    return buildAnnotatedString {
        append(text)

        // Keywords
        rules.keywords.forEach { keyword ->
            "\\b$keyword\\b".toRegex().findAll(text).forEach { match ->
                addStyle(
                    SpanStyle(color = keywordColor),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }

        // Comments
        rules.comments.forEach { comment ->
            Regex("${Regex.escape(comment)}.*").findAll(text).forEach { match ->
                addStyle(
                    SpanStyle(color = Color(0xFF6A9955)),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }

        // Strings
        val stringRegex = Regex("\".*?\"|'.*?'")
        stringRegex.findAll(text).forEach { match ->
            addStyle(
                SpanStyle(color = Color(0xFFD69D85)),
                match.range.first,
                match.range.last + 1
            )
        }

        // Numbers
        val numberRegex = Regex("\\b\\d+\\b")
        numberRegex.findAll(text).forEach { match ->
            addStyle(
                SpanStyle(color = Color(0xFFB5CEA8)),
                match.range.first,
                match.range.last + 1
            )
        }
    }
}
