package com.example.flagers.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val PREFS_NAME = "flagers_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NICKNAME = "nickname"
    }

    fun getUserId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var id = prefs.getString(KEY_USER_ID, null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_USER_ID, id).apply()
        }
        return id
    }

    fun getNickname(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_NICKNAME, "") ?: ""
    }

    fun saveNickname(context: Context, nickname: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_NICKNAME, nickname.trim()).apply()    }

    fun hasPlayedDailyToday(context: Context, date: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("daily_played_$date", false)
    }

    fun setPlayedDailyToday(context: Context, date: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("daily_played_$date", true).apply()
    }

    suspend fun saveDailyResult(
        context: Context,
        date: String,
        won: Boolean,
        attemptsUsed: Int,
        countryName: String
    ) {
        val userId = getUserId(context)
        val nickname = getNickname(context).ifEmpty { "Anonymous" }

        val result = hashMapOf(
            "userId" to userId,
            "nickname" to nickname,
            "date" to date,
            "won" to won,
            "attemptsUsed" to attemptsUsed,
            "countryName" to countryName,
            "timestamp" to System.currentTimeMillis()
        )

        try {
            val docId = "${userId}_$date"
            db.collection("daily_results").document(docId).set(result).await()
            setPlayedDailyToday(context, date)
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to save daily result", e)
        }
    }

    suspend fun getDailyLeaderboard(date: String): List<DailyResult> {
        return try {
            val snapshot = db.collection("daily_results")
                .whereEqualTo("date", date)
                .get()
                .await()

            val results = snapshot.documents.mapNotNull { doc ->
                val userId = doc.getString("userId") ?: ""
                val nickname = doc.getString("nickname") ?: "Anonymous"
                val won = doc.getBoolean("won") ?: false
                val attemptsUsed = (doc.get("attemptsUsed") as? Number)?.toInt() ?: 0
                val countryName = doc.getString("countryName") ?: ""
                val timestamp = (doc.get("timestamp") as? Number)?.toLong() ?: 0L

                DailyResult(userId, nickname, date, won, attemptsUsed, countryName, timestamp)
            }

            results.sortedWith(
                compareByDescending<DailyResult> { it.won }
                    .thenBy { it.attemptsUsed }
                    .thenBy { it.timestamp }
            )
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to get leaderboard", e)
            emptyList()
        }
    }
}
