package com.richbars.moraisdabet.infrastructure.adapter.out

import com.richbars.moraisdabet.core.application.port.FulltraderHttpPort
import com.richbars.moraisdabet.infrastructure.http.HttpClientManager
import com.richbars.moraisdabet.infrastructure.util.toJson
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FulltraderHttpAdapter : FulltraderHttpPort {

    val log = LoggerFactory.getLogger(FulltraderHttpAdapter::class.java)
    private val httpClientManager = HttpClientManager()

    private val filters = mapOf(
        "Com Dados" to 351810,
        "Over HT Rodrigo" to 140208,
        "Over 0.5 FT - CASA" to 140209,
        "Over 0.5 HT" to 440587,
        "Over 0.5 FT" to 440588,
        "Over 1.5 FT" to 440589,
        "Over 2.5 FT" to 440590,
        "Over 3.5 FT" to 440591,
        "Over 4.5 FT" to 440592,
        "AC Baixa" to 499858
    )

    override suspend fun login(): String {
        val url = "https://authapi.fulltraderapps.com/auth/login"

        val payload = JSONObject(
            mapOf(
                "username" to System.getenv("EMAIL"),
                "password" to System.getenv("PASSWORD"),
                "recaptcha" to "fulltrader"
            )
        ).toString()

        val headers = mapOf("Content-Type" to "application/json")

        val response = httpClientManager.post(url, headers, payload).toJson()

        val token = response.optString("access_token")
        return token ?: throw Exception("It was not possible to retrieve the token.")
    }

    override suspend fun getEventIdsToGoltrix(): Map<String, MutableList<String>> {

        val eventIds = mutableMapOf<String, MutableList<String>>()
        val url = "https://gamesapi.fulltraderapps.com/games/live"
        val token = login()
        val headers = mapOf(
            "accept" to "application/json, text/plain, */*",
            "authorization" to "Bearer $token"
        )

        return try {

            for ((filterName, filterId) in filters) {
                val queryParams = mapOf(
                    "orderBy" to "",
                    "orderType" to "",
                    "favorites" to "",
                    "pageSize" to "100",
                    "filter" to filterId.toString()
                )

                val result = httpClientManager.get(
                    url = url,
                    headers = headers,
                    queryParams = queryParams,
                    forceTLS = true
                ).toJson()

                val games = result.optJSONArray("games") ?: continue

                for (i in 0 until games.length()) {
                    val g = games.getJSONArray(i)
                    val eventId = g.optString(13, null) ?: continue
                    if (eventId.isNotEmpty()) {
                        eventIds.getOrPut(eventId) { mutableListOf() }.add(filterName)
                    }
                }
            }

            if (eventIds.isNotEmpty()) {
//                log.debug("Total number of events collected: {}", eventIds.size)
            }

            eventIds

        } catch (e: Exception) {
            log.error("Error retrieving events in Goltrix: ${e.message}", e)
            emptyMap()
        }

    }
}