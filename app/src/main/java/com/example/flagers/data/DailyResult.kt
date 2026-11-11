package com.example.flagers.data

data class DailyResult(
    val userId: String = "",
    val nickname: String = "",
    val date: String = "",
    val won: Boolean = false,
    val attemptsUsed: Int = 0,
    val countryName: String = "",
    val timestamp: Long = 0L
)
