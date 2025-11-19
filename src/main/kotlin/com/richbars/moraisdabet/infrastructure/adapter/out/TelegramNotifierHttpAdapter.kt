package com.richbars.moraisdabet.infrastructure.adapter.out

import com.richbars.moraisdabet.core.application.port.TelegramNotifierPort
import com.richbars.moraisdabet.infrastructure.http.HttpClientManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.slf4j.LoggerFactory

class TelegramNotifierHttpAdapter : TelegramNotifierPort {

    private val url = "https://api.telegram.org/bot${System.getenv("TELEGRAM_BOT_TOKEN")}/sendMessage"
    private val chatId = System.getenv("TELEGRAM_CHAT_ID")
    private val httpClientManager = HttpClientManager()
    private val log = LoggerFactory.getLogger("[TelegramNotifier]")

    override suspend fun sendMessage(text: String): Boolean = withContext(Dispatchers.IO) {

        val payload = JSONObject().apply {
            put("chat_id", chatId)
            put("text", text)
            put("parse_mode", "MarkdownV2")
            put("disable_web_page_preview", true)
        }
        val jsonBody = payload.toString()

        try {

            val response = httpClientManager.post(url, body=jsonBody)
            val responseBody = response.body?.string() ?: ""

            when (response.code){

                200 -> {
                    log.info("Message sent successfully ✅ | TEXT: $text")
                    return@withContext true
                }

                429 -> {
                    val retryAfter = JSONObject(responseBody)
                        .optJSONObject("parameters")
                        ?.optInt("retry_after", 5) ?: 5

                    log.warn("Rate limit reached. Resending in ${retryAfter}s...")
                    kotlinx.coroutines.delay((retryAfter * 1000).toLong())
                    return@withContext sendMessage(text)
                }

                else -> {
                    log.error("Failed to send message: $responseBody | Status = ${response.code}")
                    return@withContext false
                }

            }

        } catch (e: Exception) {
            println(e)
        }

        return@withContext true

    }

}