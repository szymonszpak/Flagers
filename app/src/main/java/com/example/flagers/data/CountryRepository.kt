package com.example.flagers.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CountryRepository(private val context: Context) {

    fun loadCountries(): List<Country> {
        return try {
            val jsonString = context.assets.open("countries.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<Country>>() {}.type
            Gson().fromJson(jsonString, listType)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getRandomCountry(): Country? {
        val countries = loadCountries()
        return if (countries.isNotEmpty()) countries.random() else null
    }
}