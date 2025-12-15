package com.richbars.moraisdabet.core.application.port

import com.richbars.moraisdabet.core.application.dto.ChardrawDto
import com.richbars.moraisdabet.core.application.dto.TelegramGoltrixDto

interface TelegramNotifierPort {
    suspend fun sendMessageGoltrix(telegramGoltrixDto: TelegramGoltrixDto): Boolean
    suspend fun sendMessageChardraw(games: List<ChardrawDto>)
    suspend fun sendTextMessage(message: String): Boolean
}