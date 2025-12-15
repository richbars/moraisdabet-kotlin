package com.richbars.moraisdabet.infrastructure.adapter.out

import com.richbars.moraisdabet.core.application.dto.ChardrawDto
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class TelegramNotifierHttpAdapter : TelegramNotifierPort {

    private val url = "https://api.telegram.org/bot${System.getenv("TELEGRAM_BOT_TOKEN")}/sendMessage"
    private val chatId = System.getenv("TELEGRAM_CHAT_ID")
    private val httpClientManager = HttpClientManager()
    private val log = LoggerFactory.getLogger(TelegramNotifierHttpAdapter::class.java)

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

    override suspend fun sendTextMessage(message: String): Boolean =
        withContext(Dispatchers.IO) {

            val payload = JSONObject().apply {
                put("chat_id", chatId)
                put("text", message)
                put("parse_mode", "MarkdownV2")
                put("disable_web_page_preview", true)
            }

            val jsonBody = payload.toString()

            try {
                val response = httpClientManager.post(url, body = jsonBody)
                val responseBody = response.body?.string() ?: ""

                when (response.code) {
                    200 -> {
                        log.info("Text message sent successfully ✅")
                        return@withContext true
                    }

                    429 -> {
                        val retryAfter = JSONObject(responseBody)
                            .optJSONObject("parameters")
                            ?.optInt("retry_after", 5) ?: 5

                        log.warn("Rate limit reached. Retrying in ${retryAfter}s...")
                        delay(retryAfter * 1000L)

                        return@withContext sendTextMessage(message)
                    }

                    else -> {
                        log.error("Failed to send text message: $responseBody | Status = ${response.code}")
                        return@withContext false
                    }
                }
            } catch (e: Exception) {
                log.error("Exception while sending text message", e)
                return@withContext false
            }
        }


    override suspend fun sendMessageChardraw(games: List<ChardrawDto>) =
        withContext(Dispatchers.IO) {

            if (games.isEmpty()) {
                log.warn("Chardraw list is empty — nothing to send.")
                return@withContext
            }

            val todayRaw = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            val today = escape(todayRaw)

            val finalMessage = StringBuilder()
            finalMessage.append("*📋 Lista Chardraw $today*\n\n")

            for ((index, game) in games.withIndex()) {
                try {
                    val number = index + 1

                    val eventUrl =
                        "https://www.betfair.bet.br/exchange/plus/football/event/${game.betfairId}"

                    val marketUrl =
                        "https://www.betfair.bet.br/exchange/plus/football/market/${game.marketIdHT}"

                    finalMessage.append(
                        """
                        *\#${number}*
                        🏆 *${escape(game.leagueName)}*
                        ⚔️ *[${escape(game.homeName)} vs ${escape(game.awayName)}]($eventUrl)*
                        💰 *Odd:* ${escape(game.marketOddHT!!)}
                        ⏳ *Hora:* ${escape(game.hour.toString())}
                        🔗 *[Abrir Mercado]($marketUrl)*
                        
                        """.trimIndent() + "\n"
                    )

                } catch (e: Exception) {
                    log.warn("Failed to build Chardraw entry at index $index. Skipping. Error: ${e.message}", e)
                }
            }

            sendTextMessage(finalMessage.toString().trimEnd())
            log.debug(finalMessage.toString().trimEnd())
        }


    // --------- Helpers ---------

    fun escape(text: String): String {
        val chars = listOf(
            "_", "*", "[", "]", "(", ")", "~", "`", ">", "#", "+",
            "-", "=", "|", "{", "}", ".", "!"
        )

        var out = text
        for (c in chars) {
            out = out.replace(c, "\\$c")
        }
        return out
    }


    fun escapeMarkdownV2(text: String): String {
        val toEscape = listOf("_", "*", "[", "]", "(", ")", "~", "`", ">", "#", "+", "-", "=", "|", "{", "}", ".", "!")
        var escaped = text
        toEscape.forEach { char ->
            escaped = escaped.replace(char, "\\$char")
        }
        return escaped
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
