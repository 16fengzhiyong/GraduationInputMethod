package com.nuc.omeletteinputmethod.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nuc.omeletteinputmethod.ui.keyboard.KeyboardState
import com.nuc.omeletteinputmethod.ui.keyboard.KeyboardViewModel
import com.nuc.omeletteinputmethod.ui.keyboard.PinyinProcessor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinyinSettingsScreen(
    viewModel: KeyboardViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("拼音设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SectionTitle("拼音输入")

            // 模糊音总开关
            SwitchRow(
                title = "模糊音",
                subtitle = "开启后可识别方言口音对应的拼音",
                checked = state.fuzzyEnabled,
                onCheckedChange = { viewModel.setFuzzyEnabled(it) }
            )

            // 各模糊规则独立复选框
            if (state.fuzzyEnabled) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "模糊规则",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )

                val allRules = listOf(
                    "zh↔z" to "zh ↔ z (平翘舌)",
                    "ch↔c" to "ch ↔ c (平翘舌)",
                    "sh↔s" to "sh ↔ s (平翘舌)",
                    "n↔l" to "n ↔ l (鼻边音)",
                    "r↔l" to "r ↔ l",
                    "ang↔an" to "ang ↔ an (前后鼻音)",
                    "eng↔en" to "eng ↔ en (前后鼻音)",
                    "ing↔in" to "ing ↔ in (前后鼻音)",
                    "h↔f" to "h ↔ f (部分方言)"
                )

                allRules.forEach { (rule, label) ->
                    SwitchRow(
                        title = label,
                        checked = rule in state.fuzzyRules,
                        onCheckedChange = { viewModel.setFuzzyRule(rule, it) },
                        indent = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 拼音纠错开关
            SwitchRow(
                title = "拼音纠错",
                subtitle = "自动纠正输入中的拼写错误",
                checked = state.autoCorrect,
                onCheckedChange = { viewModel.setAutoCorrect(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle("双拼输入")

            // 双拼模式开关
            SwitchRow(
                title = "双拼模式",
                subtitle = "击键两次输入一个汉字音节",
                checked = state.doublePinyin,
                onCheckedChange = { viewModel.setDoublePinyin(it) }
            )

            // 方案选择下拉
            if (state.doublePinyin) {
                DoubleSchemeDropdown(
                    currentScheme = state.doubleScheme,
                    onSchemeSelected = { viewModel.setDoubleScheme(it) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle("混合输入")

            // 中英文混输开关
            SwitchRow(
                title = "中英文混输",
                subtitle = "无需切换模式，自动识别英文并提交",
                checked = state.mixedInput,
                onCheckedChange = { viewModel.setMixedInput(it) }
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
    indent: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (indent) 32.dp else 0.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DoubleSchemeDropdown(
    currentScheme: String,
    onSchemeSelected: (String) -> Unit
) {
    val schemeLabels = mapOf(
        "xiaohe" to "小鹤双拼",
        "ms" to "微软双拼",
        "ziranma" to "自然码"
    )

    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "双拼方案",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            Row(
                modifier = Modifier
                    .menuAnchor()
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = schemeLabels[currentScheme] ?: currentScheme,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                schemeLabels.forEach { (scheme, label) ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = label, modifier = Modifier.weight(1f))
                                if (scheme == currentScheme) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onSchemeSelected(scheme)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}