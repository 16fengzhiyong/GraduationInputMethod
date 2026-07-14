package com.nuc.omeletteinputmethod.ui.translate

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TranslateScreen(
    viewModel: TranslateViewModel = viewModel()
) {
    var inputText by remember { mutableStateOf("") }
    val result by viewModel.translationResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Translate",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Enter text to translate") },
            modifier = Modifier.fillMaxWidth().height(150.dp),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.translate(inputText) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Translate (Auto -> ZH)")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Result:", style = MaterialTheme.typography.titleMedium)
        
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(top = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (result.isEmpty()) "Translation will appear here..." else result,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
