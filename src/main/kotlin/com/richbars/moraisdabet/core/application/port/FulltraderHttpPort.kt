package com.richbars.moraisdabet.core.application.port

import org.json.JSONArray

interface FulltraderHttpPort {
    suspend fun login(): String
    suspend fun getEventIdsToGoltrix(): Map<String, MutableList<String>>
    suspend fun getGamesChardraw(): JSONArray
}