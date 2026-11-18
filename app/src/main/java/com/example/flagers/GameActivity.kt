package com.example.flagers

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.flagers.ui.game.GameMode
import com.example.flagers.ui.game.GameScreen
import com.example.flagers.ui.theme.FlagersTheme

class GameActivity : ComponentActivity() {

    companion object {
        const val EXTRA_GAME_MODE = "EXTRA_GAME_MODE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val modeName = intent.getStringExtra(EXTRA_GAME_MODE) ?: GameMode.CLASSIC.name
        val gameMode = try {
            GameMode.valueOf(modeName)
        } catch (e: Exception) {
            GameMode.CLASSIC
        }

        setContent {
            FlagersTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameScreen(
                        gameMode = gameMode,
                        onBackClick = { finish() },
                        onLeaderboardClick = {
                            val intent = Intent(this@GameActivity, LeaderboardActivity::class.java)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}
