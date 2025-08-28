package com.example.codecraft

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuBar(
    currentFileName: String,
    context: Context,
    onNewFile: (String) -> Unit,
    onOpenFile: (String) -> Unit,
    onSaveFile: (String) -> Unit
) {
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showOpenFileDialog by remember { mutableStateOf(false) }
    var showSaveFileDialog by remember { mutableStateOf(false) }
    var fileName by remember { mutableStateOf("Untitled") }
    val extensions = listOf(".kt", ".txt", ".java", ".py", ".js")
    var selectedExtension by remember { mutableStateOf(extensions.first()) }

    // Top App Bar with menu
    TopAppBar(
        title = {
            Text(
                text = "CodeCraft - $currentFileName",
                style = MaterialTheme.typography.titleMedium
            )
        },
        actions = {
            // New File Button
            IconButton(onClick = { showNewFileDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New File",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Open File Button
            IconButton(onClick = { showOpenFileDialog = true }) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = "Open File",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Save File Button
            IconButton(onClick = { showSaveFileDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save File",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )

    // New File Dialog
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Create New File", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        label = { Text("File Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Extension: ${selectedExtension}")
                        Button(
                            onClick = {
                                val currentIndex = extensions.indexOf(selectedExtension)
                                val nextIndex = (currentIndex + 1) % extensions.size
                                selectedExtension = extensions[nextIndex]
                            }
                        ) {
                            Text("Change")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val finalName = if (fileName.endsWith(selectedExtension)) {
                            fileName
                        } else {
                            fileName + selectedExtension
                        }
                        onNewFile(finalName)
                        showNewFileDialog = false
                        fileName = "Untitled"
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Open File Dialog
    if (showOpenFileDialog) {
        val files = context.filesDir.listFiles()?.toList() ?: emptyList()
        AlertDialog(
            onDismissRequest = { showOpenFileDialog = false },
            title = { Text("Open File", style = MaterialTheme.typography.titleLarge) },
            text = {
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(files.size) { index ->
                        val file = files[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onOpenFile(file.name)
                                    showOpenFileDialog = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.InsertDriveFile,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = file.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOpenFileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Save File Dialog
    if (showSaveFileDialog) {
        AlertDialog(
            onDismissRequest = { showSaveFileDialog = false },
            title = { Text("Save File", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        label = { Text("File Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Extension: ${selectedExtension}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val finalName = if (fileName.endsWith(selectedExtension)) {
                            fileName
                        } else {
                            fileName + selectedExtension
                        }
                        onSaveFile(finalName)
                        showSaveFileDialog = false
                        fileName = "Untitled"
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveFileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}