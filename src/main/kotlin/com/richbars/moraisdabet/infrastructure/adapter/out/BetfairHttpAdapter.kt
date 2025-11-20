package com.richbars.moraisdabet.infrastructure.adapter.out

import com.richbars.moraisdabet.core.application.port.BetfairHttpPort
import com.richbars.moraisdabet.core.application.dto.Back
import com.richbars.moraisdabet.core.application.dto.EventBetfairDto
import com.richbars.moraisdabet.core.application.dto.Lay
import com.richbars.moraisdabet.core.application.dto.MarketBetfairDto
import com.richbars.moraisdabet.infrastructure.http.HttpClientManager
import com.richbars.moraisdabet.infrastructure.util.toJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class BetfairHttpAdapter : BetfairHttpPort {

    private val log = LoggerFactory.getLogger(BetfairHttpAdapter::class.java)
    private val httpClientManager = HttpClientManager()

    companion object {
        private const val BASE_URL = "https://ero.betfair.bet.br/www/sports/exchange/readonly/v1"
        private const val BY_MARKET_URL = "https://ero.betfair.bet.br/www/sports/exchange/readonly/v1/bymarket"
        private const val BY_EVENT_URL = "https://ero.betfair.bet.br/www/sports/exchange/readonly/v1/byevent"
        private const val EVENT_DETAILS_URL = "https://ips.betfair.bet.br/inplayservice/v1/eventDetails"
        private val DEFAULT_HEADERS = mapOf("Accept" to "application/json")
        private val TIMEZONE: ZoneId = ZoneId.of("America/Sao_Paulo")

        // 🔹 Mapeamento dos nomes
        private val marketMap = mapOf(
            "Over 0.5 FT" to "Over/Under 0.5 Goals",
            "Over 1.5 FT" to "Over/Under 1.5 Goals",
            "Over 2.5 FT" to "Over/Under 2.5 Goals",
            "Over 3.5 FT" to "Over/Under 3.5 Goals",
            "Over 4.5 FT" to "Over/Under 4.5 Goals",
            "Over 0.5 HT" to "First Half Goals 0.5",
            "Over 0.5 FT - CASA" to "First Half Goals 0.5"
        )

    }

    /** Coletar as informações do jogo pelo eventId **/
    override suspend fun getEventById(eventId: Long): EventBetfairDto = withContext(Dispatchers.IO) {

        val params = mapOf(
            "eventIds" to eventId.toString(),
            "locale" to "en_US",
            "productType" to "EXCHANGE",
            "regionCode" to "UK"
        )

        try {

            val response = httpClientManager.get(EVENT_DETAILS_URL, DEFAULT_HEADERS, params, forceTLS = true)
            val data = response.toJson()

            val raw = data.optString("raw", "[]")
            val rawArray = JSONArray(raw)

            val firstEvent = rawArray.optJSONObject(0)
            if (firstEvent == null) {
                log.warn("Nenhum evento encontrado para o ID: $eventId")
                throw Exception("Nenhum evento encontrado")
            }

            val startTimeStr = firstEvent.optString("startTime")
            val startTime = ZonedDateTime.parse(startTimeStr, DateTimeFormatter.ISO_ZONED_DATE_TIME).withZoneSameInstant(TIMEZONE)

            return@withContext EventBetfairDto(
                eventId = firstEvent.optLong("eventId"),
                eventName = firstEvent.optString("eventName"),
                league = firstEvent.optString("competitionName"),
                home = firstEvent.optString("homeName"),
                away = firstEvent.optString("awayName"),
                date = startTime.toLocalDate(),
                hour = startTime.toLocalTime()
            )

        } catch (e: Exception) {
            log.error("Erro ao buscar evento $eventId: ${e.message}")
            throw Exception("Erro ao buscar evento $eventId: ${e.message}")
        }
    }

    override suspend fun getMarketById(eventId: Long, alertName: String): MarketBetfairDto? = withContext(Dispatchers.IO){

        val params = mapOf(
            "eventIds" to eventId.toString(),
            "locale" to "en_US",
            "types" to "MARKET_STATE,EVENT,MARKET_DESCRIPTION",
            "currencyCode" to "BRL"
        )

        var overUnderMarket: JSONObject? = null
        var firstHalfMarket: JSONObject? = null

        try {

            val result = httpClientManager.get(BY_EVENT_URL, DEFAULT_HEADERS, params, true)

            val eventTypes = result.toJson().optJSONArray("eventTypes")
                ?: throw Exception("No markets available for betfairId: $eventId")

            val marketNodes = eventTypes.optJSONObject(0).optJSONArray("eventNodes").optJSONObject(0).optJSONArray("marketNodes")
            val marketList = (0 until marketNodes.length()).mapNotNull { marketNodes.optJSONObject(it) }

            // 🔹 Filtra mercados
            val overUnderMarkets = marketList.filter {
                it.optJSONObject("description")?.optString("marketName")?.contains("Over/Under") == true
            }

            val firstHalfGoalsMarkets = marketList.filter {
                it.optJSONObject("description")?.optString("marketName")?.contains("First Half Goals") == true
            }

            if (overUnderMarkets.isEmpty()) {
                throw Exception("No Over/Under markets found for event $eventId")
            }


            // Choose Market
            if (alertName in listOf("Over HT Rodrigo", "Com Dados", "AC Baixa")) {
                overUnderMarket = overUnderMarkets.minByOrNull {
                    val regex = Regex("Over/Under\\s([\\d.]+)\\sGoals")
                    val match = regex.find(it.optJSONObject("description")?.optString("marketName") ?: "")
                    match?.groups?.get(1)?.value?.toDoubleOrNull() ?: Double.MAX_VALUE
                }
            }
            else if (marketMap.containsKey(alertName)) {
                val targetName = marketMap[alertName]
                if (targetName?.contains("First Half") == true) {
                    firstHalfMarket = firstHalfGoalsMarkets.firstOrNull {
                        it.optJSONObject("description")?.optString("marketName") == targetName
                    }
                } else {
                    overUnderMarket = overUnderMarkets.firstOrNull {
                        it.optJSONObject("description")?.optString("marketName") == targetName
                    }
                }
            }

            // Search Infos to Over/Under market
            val lay = overUnderMarket?.let { createLayFromMarketNode(it) }
            val back = firstHalfMarket?.let { createBackFromMarketNode(it) }

            val res = MarketBetfairDto(lay, back)


            return@withContext res

        } catch (e: Exception) {
            log.error("Erro ao buscar os mercados do evento $eventId -> ${e.message}")
            return@withContext null
        }

    }

    override suspend fun getStatusMarketById(marketId: String): String =
        withContext(Dispatchers.IO) {

            val params = mapOf(
                "currencyCode" to "BRL",
                "locale" to "en_US",
                "marketIds" to marketId.toString(),
                "types" to listOf(
                    "MARKET_STATE",
                    "MARKET_RATES",
                    "MARKET_DESCRIPTION",
                    "EVENT",
                    "RUNNER_DESCRIPTION",
                    "RUNNER_STATE",
                    "RUNNER_EXCHANGE_PRICES_BEST",
                    "RUNNER_METADATA",
                    "MARKET_LICENCE",
                    "MARKET_LINE_RANGE_INFO"
                ).joinToString(",")
            )

            try {
                val result = httpClientManager.get(
                    BY_MARKET_URL,
                    DEFAULT_HEADERS,
                    params,
                    true
                ).toJson()

                val eventTypes = result.optJSONArray("eventTypes")
                    ?: throw Exception("eventTypes not found for market $marketId")

                if (eventTypes.length() == 0)
                    throw Exception("Market list empty for marketId $marketId")

                val status = eventTypes
                    .optJSONObject(0)
                    ?.optJSONArray("eventNodes")?.optJSONObject(0)
                    ?.optJSONArray("marketNodes")?.optJSONObject(0)
                    ?.optJSONArray("runners")?.optJSONObject(1)
                    ?.optJSONObject("state")
                    ?.optString("status")

                if (status.isNullOrBlank())
                    throw Exception("Status not found in marketId $marketId")

                return@withContext status

            } catch (e: Exception) {
                log.error("Erro ao buscar os mercados do evento $marketId -> ${e.message}")
                throw Exception("Erro ao buscar os mercados do evento $marketId -> ${e.message}")
            }
        }



    // Create Object Lay
    private suspend fun createLayFromMarketNode(marketNode: JSONObject): Lay? {

        val marketId = marketNode.optString("marketId")
        val marketNodes = getMarketById(marketId)

        val marketName = marketNode
            .optJSONObject("description")
            ?.optString("marketName")
            ?.takeIf { it.isNotEmpty() }

        val layPrice = marketNodes
            .optJSONArray("runners")?.optJSONObject(0)
            ?.optJSONObject("exchange")
            ?.optJSONArray("availableToLay")
            ?.optJSONObject(0)
            ?.optDouble("price")
            ?.takeIf { it > 0 }

        return if (marketName != null && layPrice != null) {
            Lay(
                marketName = marketName,
                marketId = marketId,
                marketOdd = layPrice.toString()
            )
        } else {
            null
        }
    }

    // Create Object Back
    private suspend fun createBackFromMarketNode(marketNode: JSONObject): Back? {

        val marketId = marketNode.optString("marketId")
        val marketNodes = getMarketById(marketId)

        val marketName = marketNode
            .optJSONObject("description")
            ?.optString("marketName")
            ?.takeIf { it.isNotEmpty() }

        val backPrice = marketNodes
            .optJSONArray("runners")?.optJSONObject(0)
            ?.optJSONObject("exchange")
            ?.optJSONArray("availableToBack")
            ?.optJSONObject(0)
            ?.optDouble("price")
            ?.takeIf { it > 0 }

        return if (marketName != null && backPrice != null) {
            Back(
                marketName = marketName,
                marketId = marketId,
                marketOdd = backPrice.toString()
            )
        } else {
            null
        }
    }

    private fun extractGoalNumber(pattern: Regex, name: String): Double {
        return pattern.find(name)?.groupValues?.getOrNull(1)?.toDoubleOrNull() ?: Double.POSITIVE_INFINITY
    }

    private suspend fun getMarketById(marketId: String): JSONObject = withContext(Dispatchers.IO) {
        val params = mapOf(
            "currencyCode" to "BRL",
            "locale" to "en_US",
            "marketIds" to marketId,
            "types" to listOf(
                "MARKET_STATE",
                "MARKET_RATES",
                "MARKET_DESCRIPTION",
                "EVENT",
                "RUNNER_DESCRIPTION",
                "RUNNER_STATE",
                "RUNNER_EXCHANGE_PRICES_BEST",
                "RUNNER_METADATA",
                "MARKET_LICENCE",
                "MARKET_LINE_RANGE_INFO"
            ).joinToString(",")
        )

        try{
            val result = httpClientManager.get(
                BY_MARKET_URL,
                DEFAULT_HEADERS,
                params,
                true
            ).toJson()
            return@withContext result.optJSONArray("eventTypes").optJSONObject(0).optJSONArray("eventNodes").optJSONObject(0).optJSONArray("marketNodes").optJSONObject(0)
        } catch (e: Exception) {
            throw Exception("Error fetching markets $marketId: ${e.message}", e)
        }
    }



}