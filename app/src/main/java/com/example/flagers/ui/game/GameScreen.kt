package com.example.flagers.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flagers.ui.components.PieChartCanvas

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Flagers",
            style = MaterialTheme.typography.headlineLarge
        )

        uiState.currentCountry?.let { country ->
            PieChartCanvas(
                colors = country.colors,
                size = 220.dp
            )
        }

        if (uiState.gameStatus == GameStatus.IN_PROGRESS) {
            OutlinedTextField(
                value = uiState.currentGuess,
                onValueChange = { viewModel.onGuessChange(it) },
                label = { Text("Wpisz nazwę państwa") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.submitGuess() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Zgadnij")
            }
        } else {
            Button(
                onClick = { viewModel.startNewGame() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Zagraj ponownie")
            }
        }

        if (uiState.message.isNotEmpty()) {
            Text(
                text = uiState.message,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (uiState.unlockedHints.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Podpowiedzi:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(uiState.unlockedHints) { hint ->
                            Text(text = "• $hint")
                        }
                    }
                }
            }
        }
    }
}