package com.richbars.moraisdabet.infrastructure.adapter.out

import com.richbars.moraisdabet.core.application.port.CornerproHttpPort
import com.richbars.moraisdabet.infrastructure.util.toJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import kotlin.math.max

@Service
class CornerproHttpAdapter(

    @Value("\${cornerpro.token}")
    private val token: String,

) : CornerproHttpPort {

    private val client = OkHttpClient()
    private val log = LoggerFactory.getLogger(CornerproHttpAdapter::class.java)

    private suspend fun getSession() : Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://cornerprobet.com/api/v3/getSession.php?token=$token")
            .get()
            .addHeader("Accept", "*/*")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    log.error("Error getSession Cornerpro, status code: ${response.code}")
                    return@withContext false
                }
                return@withContext true
            }
        } catch (e: Exception) {
            log.error("Exception getSession Cornerpro", e)
            false
        }
    }

    private suspend fun getLive(): JSONArray {

        val filtered = JSONArray()

        val mediaType = "application/json".toMediaTypeOrNull()
        val body = "{\"token\":\"$token\"}".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://cornerprobet.com/api/v3/getLive.php")
            .post(body)
            .addHeader("Accept", "*/*")
            .addHeader("Content-Type", "application/json")
            .build()

        return try {

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    log.error("Error get live matchs Cornerpro, status code: ${response.code}")
                }

                val data = response.toJson().optJSONArray("data")
                    ?: throw IllegalStateException("Cornerpro response without data array")

                for (i in 0 until data.length()){
                    val item = data.optJSONObject(i) ?: continue
                    val betfairId = item.optInt("betfairMID")
                    if (betfairId != 0) {
                        filtered.put(item)
                    }
                }

                if (filtered.length() == 0){
                    throw IllegalStateException("No live matches in Cornerpro")
                }

                filtered

            }

        } catch (e: Exception) {
            log.error("Exception get live matchs Cornerpro", e)
            throw e
        }

    }

    private suspend fun getSaveFilter(): JSONArray {
        val mediaType = "application/json".toMediaTypeOrNull()
        val body = "{\n\t\"token\": \"$token\"\n}".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://cornerprobet.com/actions/user/getSavedFilter.php")
            .post(body)
            .addHeader("Accept", "*/*")
            .addHeader("Content-Type", "application/json")
            .build()

        return try {

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    log.error("Error get live matchs Cornerpro, status code: ${response.code}")
                }

                val data = response.toJson().optJSONArray("data")
                    ?: throw IllegalStateException("Cornerpro response without data array")

                data

            }

        } catch (e: Exception){
            log.error("Exception getSaveFilter Cornerpro", e)
            throw e
        }
    }

    suspend fun extractSuperiority(pf1: String): Pair<String, Int> {
        try {
            val superiorityRegex = Regex("(\\d+)%")
            val teamRegex = Regex("<b>(.*?)</b>\\s*com\\s*<b>", RegexOption.IGNORE_CASE)

            val superiorityMatch = superiorityRegex.find(pf1)
            val teamMatch = teamRegex.find(pf1)

            if (superiorityMatch == null || teamMatch == null) {
                throw IllegalArgumentException("Pattern not found in input string")
            }

            val superiority = superiorityMatch.groupValues[1].toInt()
            val superiorTeam = teamMatch.groupValues[1]

            return Pair(superiorTeam, superiority)

        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Failed to extract superiority from pf1: $pf1 | Error: ${e.message}",
                e
            )
        }
    }

    override suspend fun getGamesMatch(): JSONArray {

        getSession()
        val gamesCorner = getLive()
        val filterCorner = runCatching {
            JSONArray(
                getSaveFilter()
                    .optJSONObject(0)
                    ?.optString("filters")
                    ?: "[]"
            )
        }.getOrElse { JSONArray() }
        val filterGames = filterGames(gamesCorner, filterCorner)
        println(filterGames)
        return filterGames
    }

    private suspend fun filterGames(
        games: JSONArray,
        filters: JSONArray
    ): JSONArray {

        val result = JSONArray()

        for (i in 0 until games.length()) {
            val game = games.optJSONObject(i) ?: continue

            if (matchFilters(game, filters)) {
                result.put(game)
            }
        }

        return result
    }

    private suspend fun matchFilters(
        game: JSONObject,
        filters: JSONArray
    ): Boolean {

        for (i in 0 until filters.length()) {
            val condition = filters.optJSONObject(i) ?: return false

            val name = condition.optString("name").lowercase()
            val valueRange = condition.optJSONArray("value") ?: return false
            val both = condition.optBoolean("both", true)

            val minVal = valueRange.optInt(0)
            val maxVal = valueRange.optInt(1)

            val value: Int = when (name) {

                "minutes" -> {
                    game.optInt("minutes", 0)
                }

                "goals" -> {
                    val currentStats = game.optJSONObject("currentStats")
                        ?: JSONObject()

                    val goals = currentStats.optJSONObject("goals")
                        ?: JSONObject()

                    goals.optInt("home", 0) + goals.optInt("away", 0)
                }

                "redcards" -> {
                    val currentStats = game.optJSONObject("currentStats")
                        ?: JSONObject()

                    val reds = currentStats.optJSONObject("redcards")
                        ?: JSONObject()

                    val home = reds.optInt("home", 0)
                    val away = reds.optInt("away", 0)

                    if (both) home + away else max(home, away)
                }

                else -> continue
            }

            if (value < minVal || value > maxVal) {
                return false
            }
        }

        return true
    }

}