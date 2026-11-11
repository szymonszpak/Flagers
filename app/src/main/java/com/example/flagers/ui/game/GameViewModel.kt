package com.example.flagers.ui.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flagers.data.Country
import com.example.flagers.data.CountryRepository
import com.example.flagers.data.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CountryRepository()
    private val userRepository = UserRepository()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun startNewGame(mode: GameMode = _uiState.value.gameMode) {
        timerJob?.cancel()
        viewModelScope.launch {
            _isLoading.value = true
            
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val todayDateString = sdf.format(Date())

            if (mode == GameMode.DAILY && userRepository.hasPlayedDailyToday(getApplication(), todayDateString)) {
                val countryToPlay = repository.getDailyCountry(todayDateString)
                _uiState.value = GameState(
                    gameMode = GameMode.DAILY,
                    currentCountry = countryToPlay,
                    gameStatus = GameStatus.ALREADY_PLAYED,
                    message = "You have already played today's Daily challenge!\nCome back tomorrow for a new flag!"
                )
                _isLoading.value = false
                return@launch
            }

            val countryToPlay = if (mode == GameMode.DAILY) {
                repository.getDailyCountry(todayDateString)
            } else {
                repository.getRandomCountry()
            }

            if (mode == GameMode.RUSH) {
                val hints = countryToPlay?.let { getRandomHints(it) } ?: emptyList()
                _uiState.value = GameState(
                    gameMode = GameMode.RUSH,
                    currentCountry = countryToPlay,
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
                    gameMode = mode,
                    currentCountry = countryToPlay,
                    attemptsLeft = 5,
                    currentGuess = "",
                    unlockedHints = emptyList(),
                    gameStatus = GameStatus.IN_PROGRESS,
                    message = "",
                    timeLeft = 0,
                    score = 0
                )
            }
            _isLoading.value = false
        }
    }

    private fun startNextRushCountry() {
        viewModelScope.launch {
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
    }

    private fun getRandomHints(country: Country): List<String> {
        val allHints = listOf(
            "Area: ${formatArea(country.area)}",
            "Population: ${formatPopulation(country.population)}",
            "Continent: ${country.continent}",
            "First letter: ${country.name.first()}"
        )
        return allHints.shuffled().take(2)
    }

    private fun formatArea(areaStr: String): String {
        val area = areaStr.replace(Regex("[^0-9]"), "").toLongOrNull() ?: return areaStr
        return "%,d".format(java.util.Locale.US, area).replace(',', ' ') + " km²"
    }

    private fun formatPopulation(popStr: String): String {
        val pop = popStr.replace(Regex("[^0-9]"), "").toLongOrNull() ?: return popStr
        return when {
            pop >= 1_000_000_000 -> {
                val formatted = String.format(java.util.Locale.US, "%.2f", pop / 1_000_000_000.0)
                "${formatted.removeSuffix(".00")} B"
            }
            pop >= 1_000_000 -> {
                val formatted = String.format(java.util.Locale.US, "%.1f", pop / 1_000_000.0)
                "${formatted.removeSuffix(".0")} M"
            }
            pop >= 1_000 -> {
                val formatted = String.format(java.util.Locale.US, "%.1f", pop / 1_000.0)
                "${formatted.removeSuffix(".0")} K."
            }
            else -> pop.toString()
        }
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
            submitClassicOrDailyGuess(userGuess, targetCountry)
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

    private fun submitClassicOrDailyGuess(userGuess: String, targetCountry: Country) {
        val currentState = _uiState.value
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val todayDateString = sdf.format(Date())

        if (userGuess.equals(targetCountry.name, ignoreCase = true)) {
            val hintsUsed = 5 - currentState.attemptsLeft
            val attemptsUsed = hintsUsed + 1
            
            _uiState.update {
                it.copy(
                    gameStatus = GameStatus.WON,
                    message = if (currentState.gameMode == GameMode.DAILY) 
                                "Congratulations! Daily country guessed in $attemptsUsed try/tries." 
                              else 
                                "Congratulations! That's correct!"
                )
            }
            
            if (currentState.gameMode == GameMode.DAILY) {
                viewModelScope.launch {
                    userRepository.saveDailyResult(
                        context = getApplication(),
                        date = todayDateString,
                        won = true,
                        attemptsUsed = attemptsUsed,
                        countryName = targetCountry.name
                    )
                }
            }
            
        } else {
            val newAttemptsLeft = currentState.attemptsLeft - 1
            val newHints = currentState.unlockedHints.toMutableList()

            when (newAttemptsLeft) {
                4 -> newHints.add("Area: ${formatArea(targetCountry.area)}")
                3 -> newHints.add("Population: ${formatPopulation(targetCountry.population)}")
                2 -> newHints.add("Continent: ${targetCountry.continent}")
                1 -> newHints.add("First letter: ${targetCountry.name.first()}")
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

                if (currentState.gameMode == GameMode.DAILY) {
                    viewModelScope.launch {
                        userRepository.saveDailyResult(
                            context = getApplication(),
                            date = todayDateString,
                            won = false,
                            attemptsUsed = 5,
                            countryName = targetCountry.name
                        )
                    }
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