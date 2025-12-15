package com.richbars.moraisdabet.core.application.port

import org.json.JSONArray

interface PackballHttpPort {
    suspend fun login(): String
}