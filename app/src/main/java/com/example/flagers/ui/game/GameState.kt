package com.example.flagers.ui.game

import com.example.flagers.data.Country

enum class GameMode {
    CLASSIC,
    RUSH,
    DAILY
}

enum class GameStatus {
    IN_PROGRESS,
    WON,
    LOST,
    ALREADY_PLAYED
}

data class GameState(
    val gameMode: GameMode = GameMode.CLASSIC,
    val currentCountry: Country? = null,
    val attemptsLeft: Int = 5,
    val currentGuess: String = "",
    val unlockedHints: List<String> = emptyList(),
    val gameStatus: GameStatus = GameStatus.IN_PROGRESS,
    val message: String = "",
    val timeLeft: Int = 60,
    val score: Int = 0
)