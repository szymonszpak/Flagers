package com.example.flagers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.flagers.ui.game.GameMode
import com.example.flagers.ui.game.GameScreen
import com.example.flagers.ui.home.HomeScreen
import com.example.flagers.ui.mode.ModeSelectionScreen
import com.example.flagers.ui.theme.FlagersTheme

enum class Screen {
    HOME,
    MODE_SELECTION,
    GAME_CLASSIC,
    GAME_RUSH
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlagersTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by rememberSaveable { mutableStateOf(Screen.HOME) }

                    when (currentScreen) {
                        Screen.HOME -> HomeScreen(
                            onStartGameClick = { currentScreen = Screen.MODE_SELECTION }
                        )
                        Screen.MODE_SELECTION -> ModeSelectionScreen(
                            onClassicClick = { currentScreen = Screen.GAME_CLASSIC },
                            onRushClick = { currentScreen = Screen.GAME_RUSH },
                            onDailyClick = { },
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
                    }
                }
            }
        }
    }
}