package com.example.flagers.ui.game

import com.example.flagers.data.Country

enum class GameStatus {
    IN_PROGRESS,
    WON,
    LOST
}

data class GameState(
    val currentCountry: Country? = null,
    val attemptsLeft: Int = 5,
    val currentGuess: String = "",
    val unlockedHints: List<String> = emptyList(),
    val gameStatus: GameStatus = GameStatus.IN_PROGRESS,
    val message: String = ""
)