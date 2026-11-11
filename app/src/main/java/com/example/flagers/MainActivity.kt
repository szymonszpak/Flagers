package com.example.flagers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.flagers.data.UserRepository
import com.example.flagers.ui.game.GameMode
import com.example.flagers.ui.game.GameScreen
import com.example.flagers.ui.home.HomeScreen
import com.example.flagers.ui.leaderboard.LeaderboardScreen
import com.example.flagers.ui.mode.ModeSelectionScreen
import com.example.flagers.ui.theme.FlagersTheme

enum class Screen {
    HOME,
    MODE_SELECTION,
    GAME_CLASSIC,
    GAME_RUSH,
    GAME_DAILY,
    LEADERBOARD
}

class MainActivity : ComponentActivity() {
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FlagersTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by rememberSaveable { mutableStateOf(Screen.HOME) }
                    var showNicknameDialog by remember { 
                        mutableStateOf(userRepository.getNickname(this@MainActivity).isEmpty()) 
                    }
                    var inputNickname by remember { mutableStateOf("") }

                    if (showNicknameDialog) {
                        AlertDialog(
                            onDismissRequest = { },
                            title = { Text("Welcome to Flagers!") },
                            text = {
                                Column {
                                    Text("Enter your nickname to save your Daily stats:")
                                    OutlinedTextField(
                                        value = inputNickname,
                                        onValueChange = { inputNickname = it },
                                        singleLine = true,
                                        label = { Text("Nickname") }
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        if (inputNickname.trim().isNotEmpty()) {
                                            userRepository.saveNickname(this@MainActivity, inputNickname.trim())
                                            showNicknameDialog = false
                                        }
                                    }
                                ) {
                                    Text("Save")
                                }
                            }
                        )
                    }

                    when (currentScreen) {
                        Screen.HOME -> HomeScreen(
                            onStartGameClick = { currentScreen = Screen.MODE_SELECTION },
                            onLeaderboardClick = { currentScreen = Screen.LEADERBOARD }
                        )
                        Screen.MODE_SELECTION -> ModeSelectionScreen(
                            onClassicClick = { currentScreen = Screen.GAME_CLASSIC },
                            onRushClick = { currentScreen = Screen.GAME_RUSH },
                            onDailyClick = { currentScreen = Screen.GAME_DAILY },
                            onBackClick = { currentScreen = Screen.HOME }
                        )
                        Screen.GAME_CLASSIC -> GameScreen(
                            gameMode = GameMode.CLASSIC,
                            onBackClick = { currentScreen = Screen.MODE_SELECTION }
                        )
                        Screen.GAME_RUSH -> GameScreen(
                            gameMode = GameMode.RUSH,
                            onBackClick = { currentScreen = Screen.MODE_SELECTION }
                        )
                        Screen.GAME_DAILY -> GameScreen(
                            gameMode = GameMode.DAILY,
                            onBackClick = { currentScreen = Screen.MODE_SELECTION },
                            onLeaderboardClick = { currentScreen = Screen.LEADERBOARD }
                        )
                        Screen.LEADERBOARD -> LeaderboardScreen(
                            onBackClick = { currentScreen = Screen.HOME }
                        )
                    }
                }
            }
        }
    }
}
