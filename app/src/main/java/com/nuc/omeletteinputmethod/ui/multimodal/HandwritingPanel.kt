package com.nuc.omeletteinputmethod.ui.multimodal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HandwritingPanel(
    viewModel: HandwritingViewModel,
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
            TextButton(onClick = viewModel::clearStrokes) {
                Text("清除", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
            }
            TextButton(onClick = { viewModel.recognize() }) {
                Text("识别", style = MaterialTheme.typography.labelMedium, color = colors.primary)
            }
            if (state.isRecognizing) {
                Text("识别中...", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = { viewModel.deleteLastStroke() }) {
                Text("撤销", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
            }
            TextButton(onClick = { viewModel.commitSpace() }) {
                Text("空格", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
            }
            TextButton(onClick = { viewModel.commitEnter() }) {
                Text("回车", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
            }
            TextButton(onClick = onBackToAlpha) {
                Text("拼音", style = MaterialTheme.typography.labelMedium, color = colors.primary)
            }
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.White)
                    .onSizeChanged { size ->
                        viewModel.setCanvasSize(size.width.toFloat(), size.height.toFloat())
                    }.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                viewModel.onTouchStart(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                viewModel.onTouchMove(change.position)
                            },
                            onDragEnd = {
                                viewModel.onTouchEnd()
                            },
                            onDragCancel = {
                                viewModel.onTouchEnd()
                            },
                        )
                    },
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                for (stroke in state.strokes) {
                    if (stroke.size < 2) continue
                    val path = Path()
                    path.moveTo(stroke[0].x, stroke[0].y)
                    for (i in 1 until stroke.size) {
                        path.lineTo(stroke[i].x, stroke[i].y)
                    }
                    drawPath(
                        path = path,
                        color = Color.Black,
                        style =
                            Stroke(
                                width = 8.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                            ),
                    )
                }
            }
        }

        if (state.candidates.isNotEmpty()) {
            LazyRow(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(colors.surfaceVariant)
                        .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                itemsIndexed(state.candidates) { index, candidate ->
                    val isFirst = index == 0
                    Surface(
                        onClick = { viewModel.onCandidateSelected(candidate) },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isFirst) colors.primaryContainer else colors.surface,
                        tonalElevation = if (isFirst) 2.dp else 0.dp,
                    ) {
                        Text(
                            text = candidate,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = if (isFirst) colors.onPrimaryContainer else colors.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}