package com.richbars.moraisdabet.core.application.service

import com.richbars.moraisdabet.core.application.dto.ChardrawDto
import com.richbars.moraisdabet.core.application.dto.ChardrawUpdate
import com.richbars.moraisdabet.core.application.port.*
import com.richbars.moraisdabet.infrastructure.adapter.repository.ChardrawRepository
import com.richbars.moraisdabet.infrastructure.repository.mapper.ChardrawMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext  
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ChardrawService(
    private val fulltraderHttpPort: FulltraderHttpPort,
    private val betfairHttpPort: BetfairHttpPort,
    private val sofascoreHttptPort: SofascoreHttptPort,
    private val chardrawRepository: ChardrawRepository,
    private val telegramNotifierPort: TelegramNotifierPort,
    private val sheetsServicePort: SheetsServicePort
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
                    ?: throw IllegalArgumentException("Invalid Betfair ID for index $i -> $gameArray")

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
                    marketNameHT = markets.halftime?.marketName,
                    marketIdHT = markets.halftime?.marketId,
                    marketOddHT = markets.halftime?.marketOdd,
                    gameStatus = gameStatus,
                    statusFT = null,
                    statusHT = null
                )



                val entity = ChardrawMapper.toEntity(result)

                chardrawRepository.save(entity)
                sheetsServicePort.createChardrawRow(result)
                listGamesChardrawToTelegram.add(result)

            } catch (e: Exception) {
                log.warn("Failed to process Chardraw game at index $i. Skipping. Error: ${e.message}", e)
                continue
            }
        }
        telegramNotifierPort.sendMessageChardraw(listGamesChardrawToTelegram)
    }

    override suspend fun update() = withContext(Dispatchers.IO) {
        val gamesUnfinished = chardrawRepository.getMatchsInProgress()

        if (gamesUnfinished.isEmpty()) return@withContext

        gamesUnfinished.forEach { game ->
            val betfairId = game.betfairId
            val sofascoreId = game.sofascoreId
            val marketIdHT = game.marketIdHT
            val statusFT = game.statusFT

//            val eventInfo = betfairHttpPort.getEventDetailsById(betfairId)
//            print(eventInfo)
//            println()

            try {

                val statusMarketHT = betfairHttpPort.getStatusMarketById(marketIdHT!!)
                val statusGame = sofascoreHttptPort.getStatusGameById(sofascoreId)

                if (statusMarketHT == "LOSER") {

                    //log.debug("HT market marked as LOST. Fetching FT market information for betfairId $betfairId")

                    try {

                        val marketFT = betfairHttpPort.getMarketByIdChardraw(betfairId).fulltime

                        val marketIdFT = marketFT?.marketId ?: game.marketIdFT
                        val marketOddFT = marketFT?.marketOdd ?: game.marketOddFT
                        val marketNameFT = marketFT?.marketName ?: game.marketNameFT

                        val statusMarketFT = betfairHttpPort.getStatusMarketById(marketIdFT!!)
                        game.marketIdFT

                        val updated = ChardrawUpdate(
                            betfairId,
                            marketNameFT,
                            marketOddFT,
                            marketIdFT,
                            statusGame,
                            statusMarketHT,
                            statusMarketFT
                        )

                        chardrawRepository.updateChardraw(updated)
                        sheetsServicePort.updateChardrawRow(updated)

                    } catch (e: Exception){
                        log.error("Error to fetch info to games in betfairId $betfairId")
                        throw e
                    }

                } else {

//                    log.debug("Status HT Market equals $statusMarketHT to game $betfairId, updating infos in database")

                    val updated = ChardrawUpdate(
                        betfairId,
                        null,
                        null,
                        null,
                        statusGame,
                        statusMarketHT,
                        null
                    )

                    chardrawRepository.updateChardraw(updated)
                    sheetsServicePort.updateChardrawRow(updated)

                }

                if (!statusFT.isNullOrBlank()) {
                    try {

                        val statusMarketFT = betfairHttpPort.getStatusMarketById(game.marketIdFT!!) //Exception?
                        val updatedx = ChardrawUpdate(betfairId, null, null,
                            null, null, null, statusMarketFT)

                        chardrawRepository.updateChardraw(updatedx)

                    } catch (e: Exception){
                        log.error("Error too fetch info to market FT in betfairId $betfairId")
                    }
                }


            } catch (e: Exception) {
                log.error("Failed to update process game at betfairId: $betfairId", e)
            }
            
        }

    }
}