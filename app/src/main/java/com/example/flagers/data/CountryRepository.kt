package com.example.flagers.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CountryRepository {

    private val db = FirebaseFirestore.getInstance()
    private var cachedCountries: List<Country> = emptyList()

    suspend fun getCountries(): List<Country> {
        if (cachedCountries.isNotEmpty()) {
            return cachedCountries
        }

        return try {
            val snapshot = db.collection("flags").get().await()

            val fetchedCountries = snapshot.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null

                val colorsList = doc.get("colors") as? List<Map<String, Any>> ?: emptyList()

                val flagColors = colorsList.mapNotNull { colorMap ->
                    val hex = colorMap["hex"] as? String
                    val percentage = (colorMap["percentage"] as? Number)?.toFloat()
                    if (hex != null && percentage != null) {
                        FlagColor(hex, percentage)
                    } else null
                }

                Country(
                    id = doc.id,
                    name = name,
                    continent = doc.getString("continent") ?: "",
                    area = doc.getString("area") ?: "",
                    population = doc.getString("population") ?: "",
                    colors = flagColors
                )
            }

            cachedCountries = fetchedCountries
            fetchedCountries

        } catch (e: Exception) {
            Log.e("CountryRepository", "Failed downloading flag", e)
            emptyList<Country>()
        }
    }

    suspend fun getRandomCountry(): Country? {
        val countries = getCountries()
        return if (countries.isNotEmpty()) countries.random() else null
    }

    suspend fun getDailyCountry(seedDate: String): Country? {
        val countries = getCountries()
        if (countries.isEmpty()) return null

        val sortedCountries = countries.sortedBy { it.name }
        
        val seed = kotlin.math.abs(seedDate.hashCode())
        
        val dailyIndex = seed % sortedCountries.size
        
        return sortedCountries[dailyIndex]
    }
}