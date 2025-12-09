package com.richbars.moraisdabet.core.application.service

import com.richbars.moraisdabet.core.application.dto.ChardrawDto
import com.richbars.moraisdabet.core.application.port.*
import com.richbars.moraisdabet.infrastructure.adapter.repository.ChardrawRepository
import com.richbars.moraisdabet.infrastructure.repository.mapper.ChardrawMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ChardrawService(
    private val fulltraderHttpPort: FulltraderHttpPort,
    private val betfairHttpPort: BetfairHttpPort,
    private val sofascoreHttptPort: SofascoreHttptPort,
    private val chardrawRepository: ChardrawRepository,
    private val telegramNotifierPort: TelegramNotifierPort
) : ChardrawServicePort {

    private val log = LoggerFactory.getLogger(ChardrawService::class.java)

    override suspend fun saveGames() {
        /**
         * Processes the list of Chardraw games by fetching external data
         * (Betfair, Sofascore, and market information), assembling a final DTO,
         * converting it into an entity, and saving it to the database.
         * If any game fails during any step of processing, it is skipped and
         * execution continues for the remaining games.
         */
        val listGamesChardraw = fulltraderHttpPort.getGamesChardraw()
        val listGamesChardrawToTelegram = ArrayList<ChardrawDto>()

        for (i in 0 until listGamesChardraw.length()) {

            try {

                val gameArray = listGamesChardraw.getJSONArray(i)

                val betfairId = gameArray.get(8)
                    .toString()
                    .toLongOrNull()
                    ?: throw IllegalArgumentException("Invalid Betfair ID for index $i")

                val event = betfairHttpPort.getEventById(betfairId)
                val sofascoreId = sofascoreHttptPort.getEventNameById(event.eventName)
                val gameStatus = sofascoreHttptPort.getStatusGameById(sofascoreId)
                val markets = betfairHttpPort.getMarketByIdChardraw(betfairId)

                val result = ChardrawDto(
                    betfairId = betfairId,
                    sofascoreId = sofascoreId,
                    eventName = event.eventName,
                    leagueName = event.league,
                    homeName = event.home,
                    awayName = event.away,
                    date = event.date,
                    hour = event.hour,
                    marketNameFT = null,
                    marketIdFT = null,
                    marketOddFT = null,
                    marketNameHT = markets.halftime.marketName,
                    marketIdHT = markets.halftime.marketId,
                    marketOddHT = markets.halftime.marketOdd,
                    gameStatus = gameStatus,
                    statusFT = null,
                    statusHT = null,
                    gameScore = null
                )

                listGamesChardrawToTelegram.add(result) //Adding to list for Telegram

                val entity = ChardrawMapper.toEntity(result)

                chardrawRepository.save(entity)

            } catch (e: Exception) {
                log.warn("Failed to process Chardraw game at index $i. Skipping. Error: ${e.message}", e)
                continue
            }
        }
        telegramNotifierPort.sendMessageChardraw(listGamesChardrawToTelegram)
    }
}