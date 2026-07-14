package com.nuc.omeletteinputmethod.ui.multimodal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VoiceInputPanel(
    viewModel: VoiceInputViewModel,
    onBackToAlpha: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val colors = MaterialTheme.colorScheme

    val infiniteTransition = rememberInfiniteTransition(label = "voicePulse")
    val pulseScale by
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(tween(600), tween(0)),
            label = "pulseScale",
        )
    val pulseAlpha by
        animateFloatAsState(
            targetValue = if (state.isListening) 0.3f else 1f,
            animationSpec = tween(200),
            label = "pulseAlpha",
        )

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(colors.surface),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage ?: "",
                color = colors.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        Box(
            modifier =
                Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        if (state.isListening) colors.primary.copy(alpha = 0.3f)
                        else colors.surfaceVariant,
                    ).pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                viewModel.startListening()
                                tryAwaitRelease()
                                viewModel.stopListening()
                            },
                        )
                    },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "\uD83C\uDF99",
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (state.recognizedText.isNotEmpty()) {
            Text(
                text = state.recognizedText,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        } else if (state.isListening) {
            Text(
                text = "正在聆听...",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextButton(onClick = {
                viewModel.cancelListening()
            }) {
                Text("取消", color = colors.onSurfaceVariant)
            }

            Button(
                onClick = { viewModel.commitText() },
                enabled = state.recognizedText.isNotEmpty(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        disabledContainerColor = colors.surfaceVariant,
                    ),
            ) {
                Text("插入")
            }

            TextButton(onClick = {
                viewModel.cancelListening()
                onBackToAlpha()
            }) {
                Text("拼音", color = colors.primary)
            }
        }
    }
}