package com.richbars.moraisdabet.core.application.port

interface FulltraderHttpPort {
    suspend fun login(): String
    suspend fun getEventIdsToGoltrix(): Map<String, MutableList<String>>
}