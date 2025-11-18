package com.richbars.moraisdabet.core.application.service

import com.richbars.moraisdabet.core.application.dto.EventBetfairDto
import com.richbars.moraisdabet.core.application.dto.GoltrixDto
import com.richbars.moraisdabet.core.application.dto.GoltrixUpdate
import com.richbars.moraisdabet.core.application.dto.MarketBetfairDto
import com.richbars.moraisdabet.core.application.port.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class GoltrixService(
    private val fulltraderHttpPort: FulltraderHttpPort,
    private val sofascoreHttpPort: SofascoreHttptPort,
    private val betfairHttpPort: BetfairHttpPort,
    private val goltrixPort: GoltrixPort,
    private val goltrixEventSenderPort: GoltrixEventSenderPort,
) {

    private val failedEvents = ConcurrentHashMap.newKeySet<String>()
    private val log = LoggerFactory.getLogger(GoltrixService::class.java)

    suspend fun execute() = supervisorScope {
        val eventIds = fulltraderHttpPort.getEventIdsToGoltrix()
        if (eventIds.isEmpty()) return@supervisorScope

        val jobs = eventIds.flatMap { (eventId, filters) ->
            filters.map { filter ->
                async(Dispatchers.IO) {
                    processEvent(eventId, filter)
                }
            }
        }

        jobs.awaitAll()
    }

    suspend fun update() = supervisorScope {
        val allMatches = goltrixPort.getMatchsInProgress()
        if (allMatches.isEmpty()) return@supervisorScope

        val jobs = allMatches.map { match ->
            async(Dispatchers.IO) {
                try {
                    processMatchUpdate(match)
                } catch (e: Exception) {
                    log.error("Error processing match ${match.betfairId}: ${e.message}")
                    null
                }
            }
        }
        jobs.awaitAll()
    }


    /** Utils - Auxiliary Functions **/
    private suspend fun processEvent(
        eventId: String,
        filter: String
    ) {
        val key = "$eventId|$filter"

        if (failedEvents.contains(key)) {
            log.debug("Ignorando evento $key — marcado como falho")
            return
        }

        try {
            // Verifica duplicidade
            val alreadyExists =
                goltrixPort.findByBetfairIdAndAlertName(eventId.toLong(), filter) != null

            if (alreadyExists) {
                log.debug("Evento já existe no banco: $eventId - $filter")
                return
            }

            log.info("Processando Evento: '$eventId', Filtro: '$filter'")

            // Betfair
            val eventInfo = betfairHttpPort.getEventById(eventId.toLong())
            val marketInfo = betfairHttpPort.getMarketById(eventInfo.eventId, filter)

            // Sofascore
            val sofascoreId = sofascoreHttpPort.getEventNameById(eventInfo.eventName)
            val score = sofascoreHttpPort.getScoreGameById(sofascoreId)
            val currentMinute = sofascoreHttpPort.getCurrentGameMinuteById(sofascoreId)
            val status = sofascoreHttpPort.getStatusGameById(sofascoreId)

            // DTO
            val goltrixdto = createGoltrixDto(
                eventInfo = eventInfo,
                sofascoreId = sofascoreId,
                marketInfo = marketInfo,
                alertName = filter,
                alertEntryMinute = currentMinute,
                alertEntryScore = score,
                gameStatus = status,
                gameFinalScore = score
            )

            val saved = goltrixPort.save(goltrixdto)

            log.info(
                "Salvo: betfairId=${goltrixdto.betfairId} - ${goltrixdto.eventName} " +
                        "- thread=${Thread.currentThread().name} - time=${System.nanoTime()}"
            )

            if (saved) {
                goltrixEventSenderPort.send(goltrixdto)
            }

        } catch (ex: Exception) {
            log.error("Erro ao processar Evento='$eventId', Filtro='$filter': ${ex.message}")
            failedEvents.add(key)
        }
    }


    private suspend fun processMatchUpdate(match: GoltrixDto) {

        try {
            val marketId = match.marketHtId ?: match.marketUnderId

            // BetfairService
            val statusMarket = betfairHttpPort.getStatusMarketById(marketId!!)

            // SofascoreService
            val scoreMatch = sofascoreHttpPort.getScoreGameById(match.sofascoreId)
            val statusMatch = sofascoreHttpPort.getStatusGameById(match.sofascoreId)

            val goltrixUpdate = updateGoltrixDto(
                match.betfairId,
                match.alertName,
                null,
                scoreMatch,
                statusMatch,
                statusMarket,
                scoreMatch
            )

            val saved = goltrixPort.updateGoltrix(goltrixUpdate)

            if (saved) goltrixEventSenderPort.sendUpdate(goltrixUpdate)

        } catch (e: Exception) {
            log.error("Error in update match betfairid: ${match.betfairId} | ${e.message}")
            goltrixPort.deleteByBetfairId(match.betfairId, match.alertName)
        }
    }


    suspend fun verifyExit(eventIds: Map<String, MutableList<String>>) {
        val allMatches = goltrixPort.findAll()

    }

    fun createGoltrixDto(
        eventInfo: EventBetfairDto,
        sofascoreId: Long,
        marketInfo: MarketBetfairDto?,
        alertName: String,
        alertEntryMinute: Int?,
        alertEntryScore: String,
        gameStatus: String,
        gameFinalScore: String
    ): GoltrixDto {

        return GoltrixDto(
            betfairId = eventInfo.eventId,
            sofascoreId = sofascoreId,
            eventName = eventInfo.eventName,
            leagueName = eventInfo.league,
            homeName = eventInfo.home,
            awayName = eventInfo.away,
            date = eventInfo.date,

            alertMarketUnderName = marketInfo?.lay?.marketName,
            alertOddUnder = marketInfo?.lay?.marketOdd,

            alertMarketHtName = marketInfo?.back?.marketName,
            alertOddHt = marketInfo?.back?.marketOdd,

            marketUnderId = marketInfo?.lay?.marketId,
            marketHtId = marketInfo?.back?.marketId,

            alertName = alertName,
            alertEntryMinute = alertEntryMinute,
            alertEntryScore = alertEntryScore,

            alertExitMinute = null,
            alertExitScore = null,

            gameStatus = gameStatus,
            goltrixStatus = null,
            gameFinalScore = gameFinalScore
        )
    }

    fun updateGoltrixDto(
        betfairId: Long,
        alertName: String,
        alertExitMinute: Int?,
        alertExitScore: String,
        gameStatus: String,
        goltrixStatus: String,
        gameFinalScore: String
    ): GoltrixUpdate {

        return GoltrixUpdate(
            betfairId = betfairId,
            alertName = alertName,
            alertExitMinute = alertExitMinute,
            alertExitScore = alertExitScore,
            gameStatus = gameStatus,
            goltrixStatus = goltrixStatus,
            gameFinalScore = gameFinalScore
        )
    }

}
