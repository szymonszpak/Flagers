package com.example.flagers

import android.content.Intent
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
import com.example.flagers.ui.home.HomeScreen
import com.example.flagers.ui.mode.ModeSelectionScreen
import com.example.flagers.ui.theme.FlagersTheme

enum class MainNavScreen {
    HOME,
    MODE_SELECTION
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
                    var currentScreen by rememberSaveable { mutableStateOf(MainNavScreen.HOME) }
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
                        MainNavScreen.HOME -> HomeScreen(
                            onStartGameClick = { currentScreen = MainNavScreen.MODE_SELECTION },
                            onLeaderboardClick = { launchLeaderboardActivity() }
                        )
                        MainNavScreen.MODE_SELECTION -> ModeSelectionScreen(
                            onClassicClick = { launchGameActivity(GameMode.CLASSIC) },
                            onRushClick = { launchGameActivity(GameMode.RUSH) },
                            onDailyClick = { launchGameActivity(GameMode.DAILY) },
                            onBackClick = { currentScreen = MainNavScreen.HOME }
                        )
                    }
                }
            }
        }
    }

    private fun launchGameActivity(mode: GameMode) {
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra(GameActivity.EXTRA_GAME_MODE, mode.name)
        }
        startActivity(intent)
    }

    private fun launchLeaderboardActivity() {
        val intent = Intent(this, LeaderboardActivity::class.java)
        startActivity(intent)
    }
}
