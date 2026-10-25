package com.example.flagers.data

data class Country(
    val id: String,
    val name: String,
    val continent: String,
    val firstLetter: String,
    val colors: List<FlagColor>
)