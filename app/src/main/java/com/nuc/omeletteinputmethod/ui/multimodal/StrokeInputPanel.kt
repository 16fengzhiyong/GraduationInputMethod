package com.nuc.omeletteinputmethod.ui.multimodal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun StrokeInputPanel(
    viewModel: StrokeInputViewModel,
    onBackToAlpha: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val colors = MaterialTheme.colorScheme

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.surface),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(colors.surfaceVariant)
                    .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (state.strokeSequence.isEmpty()) "笔画" else state.strokeSequence.map { viewModel.getStrokeLabel(it) }.joinToString(""),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onBackToAlpha) {
                Text("拼音", style = MaterialTheme.typography.labelMedium, color = colors.primary)
            }
        }

        if (state.candidates.isNotEmpty()) {
            LazyRow(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(colors.surfaceVariant)
                        .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                itemsIndexed(state.candidates.take(10)) { index, candidate ->
                    val isFirst = index == 0
                    Surface(
                        onClick = { viewModel.onCandidateSelected(candidate) },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isFirst) colors.primaryContainer else colors.surface,
                        tonalElevation = if (isFirst) 2.dp else 0.dp,
                    ) {
                        Text(
                            text = candidate,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = if (isFirst) colors.onPrimaryContainer else colors.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        val strokeKeys =
            listOf(
                StrokeKey('1', "一"),
                StrokeKey('2', "丨"),
                StrokeKey('3', "丿"),
                StrokeKey('4', "丶"),
                StrokeKey('5', "乛"),
            )

        val keySize = 50.dp
        val spacer = 8.dp

        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(spacer),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                strokeKeys.forEach { key ->
                    Surface(
                        onClick = { viewModel.onStrokeKey(key.id) },
                        shape = RoundedCornerShape(8.dp),
                        color = colors.surfaceVariant,
                        modifier = Modifier.height(keySize).width(keySize),
                    ) {
                        Box(
                            modifier = Modifier.width(keySize).height(keySize),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = key.label,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = colors.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    text = key.id.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurfaceVariant.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacer),
            ) {
                Surface(
                    onClick = { viewModel.onWildcardKey() },
                    shape = RoundedCornerShape(8.dp),
                    color = colors.surfaceVariant,
                    modifier = Modifier.height(keySize).width(keySize),
                ) {
                    Box(
                        modifier = Modifier.width(keySize).height(keySize),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "*",
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = "通配",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                Surface(
                    onClick = { viewModel.onDeleteStroke() },
                    shape = RoundedCornerShape(8.dp),
                    color = colors.surfaceVariant,
                    modifier = Modifier.height(keySize).weight(1f),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(keySize),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "⌫",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Surface(
                    onClick = { viewModel.onSpace() },
                    shape = RoundedCornerShape(8.dp),
                    color = colors.surfaceVariant,
                    modifier = Modifier.height(keySize).weight(1f),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(keySize),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "空格",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Surface(
                    onClick = { viewModel.commitEnter() },
                    shape = RoundedCornerShape(8.dp),
                    color = colors.surfaceVariant,
                    modifier = Modifier.height(keySize).weight(0.7f),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(keySize),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "↵",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

private data class StrokeKey(val id: Char, val label: String)