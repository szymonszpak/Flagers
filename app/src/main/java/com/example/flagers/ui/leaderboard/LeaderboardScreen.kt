package com.example.flagers.ui.leaderboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.flagers.data.DailyResult
import com.example.flagers.data.UserRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun LeaderboardScreen(
    onBackClick: () -> Unit
) {
    val userRepository = remember { UserRepository() }
    var results by remember { mutableStateOf<List<DailyResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val todayDateString = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        sdf.format(Date())
    }

    LaunchedEffect(Unit) {
        isLoading = true
        results = userRepository.getDailyLeaderboard(todayDateString)
        isLoading = false
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
            Text(
                text = "Daily Leaderboard",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Date: $todayDateString (UTC)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No scores recorded for today yet!\nBe the first to play!",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("#", style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(30.dp))
                        Text("Player", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        Text("Tries", style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(50.dp))
                        Text("Result", style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(60.dp))
                    }

                    HorizontalDivider()

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        itemsIndexed(results) { index, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.width(30.dp)
                                )
                                Text(
                                    text = item.nickname,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = if (item.won) "${item.attemptsUsed}" else "-",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.width(50.dp)
                                )
                                Text(
                                    text = if (item.won) "WON" else "LOST",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (item.won) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.width(60.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
