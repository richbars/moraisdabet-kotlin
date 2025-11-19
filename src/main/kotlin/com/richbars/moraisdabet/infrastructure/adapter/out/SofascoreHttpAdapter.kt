package com.richbars.moraisdabet.infrastructure.adapter.out

import com.richbars.moraisdabet.core.application.port.SofascoreHttptPort
import com.richbars.moraisdabet.infrastructure.http.HttpClientManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Service
class SofascoreHttpAdapter : SofascoreHttptPort {

    companion object {
        private val httpClientManager = HttpClientManager()
        private val log = LoggerFactory.getLogger(SofascoreHttpAdapter::class.java)
        private const val BASE_URL = "https://www.sofascore.com/api/v1/search/events"
        private val TIMEZONE: ZoneId = ZoneId.of("America/Sao_Paulo")
    }

    override suspend fun getEventNameById(query: String): Long = withContext(Dispatchers.IO) {
        try {
            val headers = mapOf(
                "accept" to "application/json, text/plain, */*",
                "accept-language" to "en-US,en;q=0.9,pt-BR;q=0.8",
                "origin" to "https://www.sofascore.com",
                "referer" to "https://www.sofascore.com/",
                "user-agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
            )

            // Extrai time da casa e visitante
            val regex = Regex("(.+?)\\s+v\\s+(.+)", RegexOption.IGNORE_CASE)
            val match = regex.find(query)
            val homeTeam = match?.groupValues?.getOrNull(1)?.trim()
            val awayTeam = match?.groupValues?.getOrNull(2)?.trim()

            log.debug("🏠 Time Casa: $homeTeam, 🏃‍♂️ Visitante: $awayTeam")

            // Queries possíveis (nome completo, depois times individuais)
            val queries = mutableListOf(query)
            homeTeam?.let { queries.add(it) }
            awayTeam?.let { queries.add(it) }

            for (q in queries) {
                log.debug("🔎 Buscando eventos Sofascore para '$q' (até 5 páginas)...")

                for (page in 0 until 5) {
                    try {
                        val params = mapOf("q" to q, "page" to page.toString())

                        val rawJson = httpClientManager.webClientGet(BASE_URL, headers, params)

                        val data = if (rawJson.isNullOrBlank()) {
                            log.debug("⚠️ Resposta vazia para '$q' página $page")
                            JSONObject()
                        } else {
                            try {
                                JSONObject(rawJson)
                            } catch (e: Exception) {
                                log.error("Erro ao parsear JSON Sofascore: ${e.message}")
                                JSONObject()
                            }
                        }

                        val results = data.optJSONArray("results")
                        if (results == null || results.length() == 0) {
                            log.debug("Nenhum resultado na página $page para query '$q'")
                            continue
                        }

                        val notStarted = mutableListOf<JSONObject>()

                        for (i in 0 until results.length()) {
                            try {
                                val r = results.optJSONObject(i) ?: continue
                                val entity = r.optJSONObject("entity") ?: continue
                                val status = entity.optJSONObject("status")?.optString("type", "") ?: continue
                                if (status == "notstarted") {
                                    notStarted.add(r)
                                }
                            } catch (e: Exception) {
                                log.warn("Erro ao processar resultado $i na página $page: ${e.message}")
                                continue
                            }
                        }

                        for (matchObj in notStarted) {
                            try {
                                val entity = matchObj.optJSONObject("entity") ?: continue
                                val startTimestamp = entity.optLong("startTimestamp", 0L)
                                if (startTimestamp == 0L) continue

                                val isToday = isToday(startTimestamp)
                                if (!isToday) continue

                                val name = entity.optString("name", "unknown")
                                val id = entity.optString("id", "")
                                log.debug("✅ Jogo válido encontrado: $name (página $page)")
                                return@withContext id.toLong()
                            } catch (e: Exception) {
                                log.warn("Erro ao processar jogo notStarted: ${e.message}")
                                continue
                            }
                        }

                    } catch (e: Exception) {
                        log.error("Erro ao processar página $page para query '$q': ${e.message}")
                        continue
                    }
                }
            }

            log.error("Nenhum jogo 'not started' encontrado hoje para '$query'")
            throw Exception("No 'not started' football matches found today for '$query'")

        } catch (e: Exception) {
            log.error("❌ Erro geral em getEventNameById para query '$query': ${e.message}", e)
            throw e
        }
    }

    /**
     * Retorna o minuto atual do jogo de futebol, considerando acréscimos.
     * Funciona para 1º tempo, 2º tempo e prorrogação.
     */
    override suspend fun getCurrentGameMinuteById(sofascoreId: Long): Int? = withContext(Dispatchers.IO) {
        val url = "https://www.sofascore.com/api/v1/event/$sofascoreId"
        val headers = mapOf("accept" to "application/json, text/plain, */*")

        try {
            val result = httpClientManager.webClientGet(url, headers)
            val rawResult = JSONObject(result)
            val event = rawResult.optJSONObject("event") ?: return@withContext null

            val status = event.optJSONObject("status") ?: JSONObject()
            val statusType = status.optString("type")

            if (statusType != "inprogress") return@withContext getFinalGameMinutes(event)

            val timeInfo = event.optJSONObject("time") ?: JSONObject()
            val periodStartTs = timeInfo.optLong("currentPeriodStartTimestamp", 0L)
            if (periodStartTs == 0L) return@withContext null

            val nowTs = System.currentTimeMillis() / 1000
            val elapsedSeconds = nowTs - periodStartTs
            var elapsedMinutes = (elapsedSeconds / 60).toInt()

            val injury1 = timeInfo.optInt("injuryTime1", 0)
            val injury2 = timeInfo.optInt("injuryTime2", 0)
            val lastPeriod = event.optString("lastPeriod", "period1")

            val minute = when (lastPeriod) {
                "period1" -> {
                    // 1º tempo
                    var min = elapsedMinutes
                    if (min > 45) min = 45 + injury1
                    min
                }
                else -> {
                    // 2º tempo
                    var min = 45 + elapsedMinutes
                    if (min > 90) min = 90 + injury2
                    min
                }
            }

            return@withContext minute
        } catch (e: Exception) {
            log.error("Error getting current game minute for id $sofascoreId: ${e.message}")
            throw Exception("Error getting current game minute for id $sofascoreId: ${e.message}")
        }
    }

    private suspend fun getFinalGameMinutes(rawResult: JSONObject): Int? {

        // startTimestamp é obrigatório — se não existir, retorna null
        val startTs = rawResult.optLong("startTimestamp")

        // changes.changeTimestamp é obrigatório
        val changes = rawResult.optJSONObject("changes") ?: return null
        if (!changes.has("changeTimestamp")) return null
        val endTs = changes.optLong("changeTimestamp")
        if (endTs == 0L) return null

        // calcula
        val elapsedSeconds = endTs - startTs
        if (elapsedSeconds <= 0) return null

        return (elapsedSeconds / 60).toInt()
    }



    override suspend fun getStatusGameById(sofascoreId: Long): String = withContext(Dispatchers.IO) {
        val url = "https://www.sofascore.com/api/v1/event/$sofascoreId"
        val headers = mapOf("accept" to "application/json, text/plain, */*")

        return@withContext try {
            val result = httpClientManager.webClientGet(url, headers)
            val rawRes = JSONObject(result)
            rawRes.optJSONObject("event").optJSONObject("status").optString("type")
        } catch (e: Exception){
            log.error("Error getting status game for id $sofascoreId: ${e.message}")
            throw Exception("Error getting status game for id $sofascoreId: ${e.message}")
        }

    }

    override suspend fun getScoreGameById(sofascoreId: Long): String = withContext(Dispatchers.IO) {
        val url = "https://www.sofascore.com/api/v1/event/$sofascoreId"
        val headers = mapOf("accept" to "application/json, text/plain, */*")

        try {
            val result = httpClientManager.webClientGet(url, headers)
            val rawRes = JSONObject(result)
            val event = rawRes.optJSONObject("event")
            val awayScore = event.optJSONObject("awayScore").optInt("current")
            val homeScore = event.optJSONObject("homeScore").optInt("current")
            return@withContext "${homeScore}-${awayScore}"
        } catch (e: Exception){
            log.error("Error getting status game for id $sofascoreId: ${e.message}")
            throw Exception("Error getting status game for id $sofascoreId: ${e.message}")
        }
    }

    /** Verifica se o timestamp é do dia atual */
    private fun isToday(timestamp: Long): Boolean {
        val eventDate = Instant.ofEpochSecond(timestamp).atZone(TIMEZONE).toLocalDate()
        val today = LocalDate.now(TIMEZONE)
        return eventDate.isEqual(today)
    }
}
