package com.richbars.moraisdabet.core.application.port
import org.json.JSONArray

interface CornerproHttpPort {

    suspend fun getGamesMatch(): JSONArray

}