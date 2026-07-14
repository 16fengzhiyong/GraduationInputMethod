package com.nuc.omeletteinputmethod.ui.shortcut

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nuc.omeletteinputmethod.ui.notepad.EmptyState
import com.nuc.omeletteinputmethod.ui.notepad.NotepadTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutListScreen(
    onEditShortcut: (Long) -> Unit,
    viewModel: ShortcutViewModel = viewModel()
) {
    val shortcuts by viewModel.shortcuts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("全部") }
    var copiedText by remember { mutableStateOf("") }
    var showCopyToast by remember { mutableStateOf(false) }

    LaunchedEffect(showCopyToast) {
        if (showCopyToast) {
            kotlinx.coroutines.delay(1500)
            showCopyToast = false
        }
    }

    val filteredShortcuts = if (selectedCategory == "全部") {
        shortcuts
    } else {
        shortcuts.filter { it.category == selectedCategory }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEditShortcut(-1L) },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "新建短语")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                NotepadTopBar(
                    title = "快捷短语",
                    showSearch = true,
                    onSearchToggle = { viewModel.toggleSearch(!isSearching) }
                )

                AnimatedVisibility(
                    visible = isSearching,
                    enter = expandVertically(spring()) + fadeIn(),
                    exit = shrinkVertically(spring()) + fadeOut()
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { viewModel.setSearch(it) },
                        placeholder = { Text("搜索短语...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        singleLine = true,
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearch("") }) {
                                    Icon(Icons.Default.Close, "清除", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 8.dp)
                            .animateContentSize(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                    )
                }

                if (categories.isNotEmpty()) {
                    CategoryChips(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onSelectCategory = { selectedCategory = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (filteredShortcuts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            message = if (searchText.isNotEmpty()) "没有找到匹配的短语" else "还没有快捷短语",
                            hint = if (searchText.isNotEmpty()) "换个关键词试试" else "点击右下角的 + 添加常用短语"
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            items = filteredShortcuts,
                            key = { _, item -> item.id }
                        ) { index, item ->
                            ShortcutCard(
                                item = item,
                                index = index,
                                onClick = { onEditShortcut(item.id) },
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("shortcut", item.content))
                                    copiedText = "已复制: ${item.label}"
                                    showCopyToast = true
                                },
                                onDelete = { viewModel.deleteShortcut(item) }
                            )
                        }
                    }
                }
            }

            CopyToast(
                text = copiedText,
                visible = showCopyToast,
                onDismiss = { showCopyToast = false },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}