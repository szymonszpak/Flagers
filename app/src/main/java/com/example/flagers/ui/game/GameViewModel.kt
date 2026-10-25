package com.example.flagers.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.flagers.data.CountryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CountryRepository(application)
    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    init {
        startNewGame()
    }

    fun startNewGame() {
        val randomCountry = repository.getRandomCountry()
        _uiState.value = GameState(
            currentCountry = randomCountry,
            attemptsLeft = 5,
            currentGuess = "",
            unlockedHints = emptyList(),
            gameStatus = GameStatus.IN_PROGRESS,
            message = ""
        )
    }

    fun onGuessChange(newGuess: String) {
        _uiState.update { it.copy(currentGuess = newGuess) }
    }

    fun submitGuess() {
        val currentState = _uiState.value
        val targetCountry = currentState.currentCountry ?: return
        val userGuess = currentState.currentGuess.trim()

        if (userGuess.isEmpty() || currentState.gameStatus != GameStatus.IN_PROGRESS) return

        if (userGuess.equals(targetCountry.name, ignoreCase = true)) {
            _uiState.update {
                it.copy(
                    gameStatus = GameStatus.WON,
                    message = "Brawo! To prawidłowa odpowiedź!"
                )
            }
        } else {
            val newAttemptsLeft = currentState.attemptsLeft - 1
            val newHints = currentState.unlockedHints.toMutableList()

            when (newAttemptsLeft) {
                4 -> newHints.add("Pierwsza litera: ${targetCountry.firstLetter}")
                3 -> newHints.add("Kontynent: ${targetCountry.continent}")
                2 -> newHints.add("Liczba liter: ${targetCountry.name.length}")
            }

            if (newAttemptsLeft <= 0) {
                _uiState.update {
                    it.copy(
                        attemptsLeft = 0,
                        gameStatus = GameStatus.LOST,
                        unlockedHints = newHints,
                        message = "Przegrana! Prawidłowy kraj to: ${targetCountry.name}"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        attemptsLeft = newAttemptsLeft,
                        unlockedHints = newHints,
                        currentGuess = "",
                        message = "Błędna odpowiedź! Pozostało prób: $newAttemptsLeft"
                    )
                }
            }
        }
    }
}