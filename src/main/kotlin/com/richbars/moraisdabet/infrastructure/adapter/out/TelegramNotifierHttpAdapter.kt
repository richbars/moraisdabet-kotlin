package com.richbars.moraisdabet.infrastructure.adapter.out

import com.richbars.moraisdabet.core.application.dto.TelegramGoltrixDto
import com.richbars.moraisdabet.core.application.port.TelegramNotifierPort
import com.richbars.moraisdabet.core.application.service.GoltrixService
import com.richbars.moraisdabet.infrastructure.http.HttpClientManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TelegramNotifierHttpAdapter : TelegramNotifierPort {

    private val url = "https://api.telegram.org/bot${System.getenv("TELEGRAM_BOT_TOKEN")}/sendMessage"
    private val chatId = System.getenv("TELEGRAM_CHAT_ID")
    private val httpClientManager = HttpClientManager()
    private val log = LoggerFactory.getLogger(GoltrixService::class.java)

    override suspend fun sendMessageGoltrix(telegramGoltrixDto: TelegramGoltrixDto): Boolean =
        withContext(Dispatchers.IO) {

            val text = buildTelegramMessage(telegramGoltrixDto)

            val payload = JSONObject().apply {
                put("chat_id", chatId)
                put("text", text)
                put("parse_mode", "MarkdownV2")
                put("disable_web_page_preview", true)
            }

            val jsonBody = payload.toString()

            try {
                val response = httpClientManager.post(url, body = jsonBody)
                val responseBody = response.body?.string() ?: ""

                when (response.code) {
                    200 -> {
                        log.info("Message sent successfully ✅ | Game = ${telegramGoltrixDto.eventName} - Alert = ${telegramGoltrixDto.alertName}")
                        return@withContext true
                    }

                    429 -> {
                        val retryAfter = JSONObject(responseBody)
                            .optJSONObject("parameters")
                            ?.optInt("retry_after", 5) ?: 5

                        log.warn("Rate limit reached. Retrying in ${retryAfter}s...")
                        delay(retryAfter * 1000L)

                        return@withContext sendMessageGoltrix(telegramGoltrixDto)
                    }

                    else -> {
                        log.error("Failed to send message: $responseBody | Status = ${response.code}")
                        return@withContext false
                    }
                }
            } catch (e: Exception) {
                log.error("Exception while sending message", e)
                return@withContext false
            }
        }

    // --------- Helpers ---------

    private fun escape(text: String?): String {
        if (text == null) return ""
        return text.replace(Regex("""([_\*\[\]\(\)\~\`\>\#\+\-\=\|\{\}\.\!])""")) { "\\${it.value}" }
    }

    private fun buildTelegramMessage(dto: TelegramGoltrixDto): String {
        return """
*Entrada Goltrix Confirmada* ✅✅✅

🎯 *Filtro:* ${escape(dto.alertName)}
🏆 *${escape(dto.leagueName)}*
⚔️ *[${escape(dto.homeName)} vs ${escape(dto.awayName)}](${escape(dto.urlGame)})*
⏱️ *Minuto:* ${escape(dto.alertEntryMinute.toString())}'
🔢 *Placar:* ${escape(dto.gameFinalScore)}
💰 *Odd:* ${escape(dto.odd)}

🔗 [Abrir Mercado](${escape(dto.urlMarket)})
        """.trimIndent()
    }
}
