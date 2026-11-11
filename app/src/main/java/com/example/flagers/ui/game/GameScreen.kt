package com.example.flagers.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flagers.ui.components.OnScreenKeyboard
import com.example.flagers.ui.components.PieChartCanvas

@Composable
fun GameScreen(
    gameMode: GameMode = GameMode.CLASSIC,
    onBackClick: () -> Unit,
    onLeaderboardClick: () -> Unit = {},
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(gameMode) {
        viewModel.startNewGame(gameMode)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onBackClick) {
                Text("Back")
            }

            if (uiState.gameMode == GameMode.RUSH) {
                Text(
                    text = "Score: ${uiState.score}",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            uiState.currentCountry?.let { country ->
                PieChartCanvas(
                    colors = country.colors,
                    size = 170.dp
                )
            }
        }

        if (uiState.gameMode == GameMode.RUSH) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Time: ${uiState.timeLeft}s",
                style = MaterialTheme.typography.titleLarge,
                color = if (uiState.timeLeft <= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.currentGuess,
            onValueChange = {},
            readOnly = true,
            label = { Text("Enter country name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.unlockedHints.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Hints:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.heightIn(max = 100.dp)
                    ) {
                        items(uiState.unlockedHints) { hint ->
                            Text(
                                text = "• $hint",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        if (uiState.message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.message,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (uiState.gameStatus == GameStatus.IN_PROGRESS) {
            OnScreenKeyboard(
                onKeyPress = { viewModel.onKeyPress(it) },
                onBackspace = { viewModel.onBackspace() },
                onSubmit = { viewModel.submitGuess() }
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("Menu")
                }
                if (uiState.gameMode == GameMode.DAILY) {
                    Button(
                        onClick = onLeaderboardClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Leaderboard")
                    }
                } else {
                    Button(
                        onClick = { viewModel.startNewGame(gameMode) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Play again")
                    }
                }
            }
        }
    }
}