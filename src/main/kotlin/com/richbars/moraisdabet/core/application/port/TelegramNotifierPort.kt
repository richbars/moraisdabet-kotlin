package com.richbars.moraisdabet.core.application.port

interface TelegramNotifierPort {
    suspend fun sendMessage(text: String): Boolean
}