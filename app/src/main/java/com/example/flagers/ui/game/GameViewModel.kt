package com.example.flagers.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flagers.data.Country
import com.example.flagers.data.CountryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CountryRepository(application)
    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun startNewGame(mode: GameMode = _uiState.value.gameMode) {
        timerJob?.cancel()
        val randomCountry = repository.getRandomCountry()

        if (mode == GameMode.RUSH) {
            val hints = randomCountry?.let { getRandomHints(it) } ?: emptyList()
            _uiState.value = GameState(
                gameMode = GameMode.RUSH,
                currentCountry = randomCountry,
                attemptsLeft = 999,
                currentGuess = "",
                unlockedHints = hints,
                gameStatus = GameStatus.IN_PROGRESS,
                message = "",
                timeLeft = 60,
                score = 0
            )
            startTimer()
        } else {
            _uiState.value = GameState(
                gameMode = GameMode.CLASSIC,
                currentCountry = randomCountry,
                attemptsLeft = 5,
                currentGuess = "",
                unlockedHints = emptyList(),
                gameStatus = GameStatus.IN_PROGRESS,
                message = "",
                timeLeft = 0,
                score = 0
            )
        }
    }

    private fun startNextRushCountry() {
        val randomCountry = repository.getRandomCountry()
        val hints = randomCountry?.let { getRandomHints(it) } ?: emptyList()
        _uiState.update {
            it.copy(
                currentCountry = randomCountry,
                currentGuess = "",
                unlockedHints = hints
            )
        }
    }

    private fun getRandomHints(country: Country): List<String> {
        val allHints = listOf(
            "Area: ${country.area}",
            "Population: ${country.population}",
            "Continent: ${country.continent}",
            "First letter: ${country.firstLetter}"
        )
        return allHints.shuffled().take(2)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeft > 0 && _uiState.value.gameStatus == GameStatus.IN_PROGRESS) {
                delay(1000L)
                _uiState.update { currentState ->
                    val newTime = currentState.timeLeft - 1
                    if (newTime <= 0) {
                        currentState.copy(
                            timeLeft = 0,
                            gameStatus = GameStatus.LOST,
                            message = "Time's up! Final score: ${currentState.score}"
                        )
                    } else {
                        currentState.copy(timeLeft = newTime)
                    }
                }
            }
        }
    }

    fun onKeyPress(char: Char) {
        _uiState.update { it.copy(currentGuess = it.currentGuess + char) }
    }

    fun onBackspace() {
        _uiState.update {
            if (it.currentGuess.isNotEmpty()) {
                it.copy(currentGuess = it.currentGuess.dropLast(1))
            } else {
                it
            }
        }
    }

    fun submitGuess() {
        val currentState = _uiState.value
        val targetCountry = currentState.currentCountry ?: return
        val userGuess = currentState.currentGuess.trim()

        if (userGuess.isEmpty() || currentState.gameStatus != GameStatus.IN_PROGRESS) return

        if (currentState.gameMode == GameMode.RUSH) {
            submitRushGuess(userGuess, targetCountry)
        } else {
            submitClassicGuess(userGuess, targetCountry)
        }
    }

    private fun submitRushGuess(userGuess: String, targetCountry: Country) {
        val currentState = _uiState.value
        if (userGuess.equals(targetCountry.name, ignoreCase = true)) {
            val newScore = currentState.score + 1
            val newTime = currentState.timeLeft + 5
            _uiState.update {
                it.copy(
                    score = newScore,
                    timeLeft = newTime,
                    message = "Correct! +5s"
                )
            }
            startNextRushCountry()
        } else {
            val newTime = maxOf(0, currentState.timeLeft - 5)
            if (newTime <= 0) {
                _uiState.update {
                    it.copy(
                        timeLeft = 0,
                        currentGuess = "",
                        gameStatus = GameStatus.LOST,
                        message = "Time's up! Final score: ${currentState.score}"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        timeLeft = newTime,
                        currentGuess = "",
                        message = "Wrong guess! -5s"
                    )
                }
            }
        }
    }

    private fun submitClassicGuess(userGuess: String, targetCountry: Country) {
        val currentState = _uiState.value
        if (userGuess.equals(targetCountry.name, ignoreCase = true)) {
            _uiState.update {
                it.copy(
                    gameStatus = GameStatus.WON,
                    message = "Congratulations! That's correct!"
                )
            }
        } else {
            val newAttemptsLeft = currentState.attemptsLeft - 1
            val newHints = currentState.unlockedHints.toMutableList()

            when (newAttemptsLeft) {
                4 -> newHints.add("Area: ${targetCountry.area}")
                3 -> newHints.add("Population: ${targetCountry.population}")
                2 -> newHints.add("Continent: ${targetCountry.continent}")
                1 -> newHints.add("First letter: ${targetCountry.firstLetter}")
            }

            if (newAttemptsLeft <= 0) {
                _uiState.update {
                    it.copy(
                        attemptsLeft = 0,
                        gameStatus = GameStatus.LOST,
                        unlockedHints = newHints,
                        message = "Game Over! The correct country was: ${targetCountry.name}"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        attemptsLeft = newAttemptsLeft,
                        unlockedHints = newHints,
                        currentGuess = "",
                        message = "Incorrect guess! Attempts remaining: $newAttemptsLeft"
                    )
                }
            }
        }
    }
}