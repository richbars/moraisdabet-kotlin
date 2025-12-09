package com.richbars.moraisdabet.infrastructure.adapter.out

import com.richbars.moraisdabet.core.application.dto.*
import com.richbars.moraisdabet.core.application.port.BetfairHttpPort
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
            "Over 0.5 FT - CASA" to "Over/Under 0.5 Goals",
            "Over 0.5 FT - VISITANTE" to "Over/Under 0.5 Goals",
            "Over 0.5 HT" to "First Half Goals 0.5",
            "Over HT Rodrigo" to "First Half",
            "Com Dados" to "First Half"
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

            val marketNodes = eventTypes
                .optJSONObject(0)
                ?.optJSONArray("eventNodes")
                ?.optJSONObject(0)
                ?.optJSONArray("marketNodes")
                ?: throw Exception("Malformed response for event $eventId")

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

            // ---------------------------------------------
            // 🔥 RULE: Return ALWAYS the FIRST First Half Goals Market
            // ---------------------------------------------
            firstHalfMarket = firstHalfGoalsMarkets.firstOrNull()

            // If First Half does not exist → fallback to Over/Under
            if (firstHalfMarket == null) {
                overUnderMarket = overUnderMarkets.firstOrNull()
            }

            // If none exist (very rare)
            if (firstHalfMarket == null && overUnderMarket == null) {
                throw Exception("No usable markets found for event $eventId")
            }

            // ---------------------------------------------
            // 🔹 Create DTO objects
            // ---------------------------------------------
            val lay = overUnderMarket?.let { createLayFromMarketNode(it) }
            val back = firstHalfMarket?.let { createBackFromMarketNode(it) }

            return@withContext MarketBetfairDto(lay, back)

        } catch (e: Exception) {
            log.error("Erro ao buscar os mercados do evento $eventId -> ${e.message}")
            return@withContext null
        }


    }

    override suspend fun getMarketByIdGoltrix(eventId: Long, alertName: String): MarketBetfairDto? =
        withContext(Dispatchers.IO) {

            val params = mapOf(
                "eventIds" to eventId.toString(),
                "locale" to "en_US",
                "types" to "MARKET_STATE,EVENT,MARKET_DESCRIPTION",
                "currencyCode" to "BRL"
            )

            val translatedMarketName = marketMap[alertName]
                ?: throw Exception("No mapping found for alertName='$alertName'")

            try {

                val result = httpClientManager.get(BY_EVENT_URL, DEFAULT_HEADERS, params, true)

                val eventTypes = result.toJson().optJSONArray("eventTypes")
                    ?: throw Exception("No markets available for betfairId: $eventId")

                val marketNodes = eventTypes
                    .optJSONObject(0)
                    ?.optJSONArray("eventNodes")
                    ?.optJSONObject(0)
                    ?.optJSONArray("marketNodes")
                    ?: throw Exception("Malformed response for event $eventId")

                val marketList = (0 until marketNodes.length()).mapNotNull { marketNodes.optJSONObject(it) }

                // Filtra pelo nome traduzido
                val matchingMarkets = marketList.filter {
                    it.optJSONObject("description")
                        ?.optString("marketName")
                        ?.contains(translatedMarketName, ignoreCase = true) == true
                }

                if (matchingMarkets.isEmpty()) {
                    throw Exception("No markets found using translated name '$translatedMarketName'")
                }

                // Ordena pelos menores valores
                val selectedMarket = matchingMarkets
                    .sortedBy { market ->
                        extractLineFromMarketName(
                            market.optJSONObject("description")?.optString("marketName")
                        ) ?: Double.MAX_VALUE
                    }
                    .first()

                val selectedBack = createBackFromMarketNode(selectedMarket)

                return@withContext MarketBetfairDto(null, selectedBack)

            } catch (e: Exception) {
                log.error("Erro ao buscar os mercados do evento $eventId -> ${e.message}")
                return@withContext null
            }
        }

    // Extrai o número final (ex: 0.5, 1.5)
    private fun extractLineFromMarketName(name: String?): Double? {
        if (name == null) return null

        val regex = Regex("""([0-9]+\.[0-9]+)""")
        val match = regex.find(name) ?: return null

        return match.value.toDouble()
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

    override suspend fun getMarketByIdChardraw(eventId: Long): MarketBetfairPeriodDto =
        withContext(Dispatchers.IO) {

            val params = mapOf(
                "eventIds" to eventId.toString(),
                "locale" to "en_US",
                "types" to "MARKET_STATE,EVENT,MARKET_DESCRIPTION",
                "currencyCode" to "BRL"
            )

            return@withContext try {

                val result = httpClientManager.get(BY_EVENT_URL, DEFAULT_HEADERS, params, true)

                val eventTypes = result.toJson().optJSONArray("eventTypes")
                    ?: throw Exception("No markets available for betfairId: $eventId")

                val marketNodes = eventTypes
                    .optJSONObject(0)
                    ?.optJSONArray("eventNodes")
                    ?.optJSONObject(0)
                    ?.optJSONArray("marketNodes")
                    ?: throw Exception("Malformed response for event $eventId")

                val marketList = (0 until marketNodes.length()).mapNotNull { marketNodes.optJSONObject(it) }

                val marketHT = marketList.first {
                    it.optJSONObject("description")
                        ?.optString("marketName")
                        .orEmpty()
                        .contains("First Half Goals 0.5", ignoreCase = true)
                }

                val marketFT = marketList.first {
                    it.optJSONObject("description")
                        ?.optString("marketName")
                        .orEmpty()
                        .contains("Over/Under 0.5 Goals")
                }

                val halftime = createObjectHalftime(marketHT)
                val fulltime = createObjectFulltime(marketFT)

                MarketBetfairPeriodDto(halftime, fulltime)

            } catch (e: Exception) {
                log.error("Error while retrieving Half-Time and Full-Time markets for eventId=$eventId", e)
                log.debug("Market not found or inactive for eventId=$eventId — match may be finished or currently in-play.")
                throw e
            }
    }


    private suspend fun createObjectHalftime(market: JSONObject): Halftime {

        val marketNodes = getMarketById(market.optString("marketId"))

        val layPrice = marketNodes
            .optJSONArray("runners")?.optJSONObject(1)
            ?.optJSONObject("exchange")
            ?.optJSONArray("availableToBack")
            ?.optJSONObject(0)
            ?.optDouble("price")
            ?.takeIf { it > 0 }

        return Halftime(
            market.optJSONObject("description").optString("marketName"),
            market.optString("marketId"),
            layPrice.toString()
        )
    }

    private suspend fun createObjectFulltime(market: JSONObject): Fulltime {

        val marketNodes = getMarketById(market.optString("marketId"))

        val layPrice = marketNodes
            .optJSONArray("runners")?.optJSONObject(1)
            ?.optJSONObject("exchange")
            ?.optJSONArray("availableToBack")
            ?.optJSONObject(0)
            ?.optDouble("price")
            ?.takeIf { it > 0 }

        return Fulltime(
            market.optJSONObject("description").optString("marketName"),
            market.optString("marketId"),
            layPrice.toString()
        )
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
            .optJSONArray("runners")?.optJSONObject(1)
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