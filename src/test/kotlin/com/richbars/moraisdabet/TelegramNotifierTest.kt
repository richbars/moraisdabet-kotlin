package com.richbars.moraisdabet

import com.ninjasquad.springmockk.MockkBean
import com.richbars.moraisdabet.core.application.dto.ChardrawDto
import com.richbars.moraisdabet.infrastructure.adapter.out.TelegramNotifierHttpAdapter
import com.richbars.moraisdabet.infrastructure.adapter.repository.ChardrawRepository
import com.richbars.moraisdabet.infrastructure.adapter.repository.GoltrixRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.LocalTime

@SpringBootTest(
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration"
    ]
)
class TelegramNotifierTest {

    @MockkBean
    lateinit var goltrixRepository: GoltrixRepository

    @MockkBean
    lateinit var chardrawRepository: ChardrawRepository

    @Autowired
    lateinit var telegramNotifierHttpAdapter: TelegramNotifierHttpAdapter

    @Test
    fun `should sendMessageChardraw execute successfully`() = runTest {

        val mockGames = listOf(
            ChardrawDto(
                betfairId = 1111111111L,
                sofascoreId = 55501,
                eventName = "Team A vs Team B",
                leagueName = "Premier League",
                homeName = "Team A",
                awayName = "Team B",
                date = LocalDate.of(2025, 1, 15),
                hour = LocalTime.of(14, 0),
                marketNameFT = null,
                marketIdFT = null,
                marketOddFT = null,
                marketNameHT = "HT Draw",
                marketIdHT = "1.251241117",
                marketOddHT = "2.45",
                gameStatus = "32'",
                statusFT = null,
                statusHT = null,
                gameScore = "1-0"
            ),
            ChardrawDto(
                betfairId = 1111111112L,
                sofascoreId = 55502,
                eventName = "Lions FC vs Eagles SC",
                leagueName = "La Liga",
                homeName = "Lions FC",
                awayName = "Eagles SC",
                date = LocalDate.of(2025, 1, 15),
                hour = LocalTime.of(16, 30),
                marketNameFT = null,
                marketIdFT = null,
                marketOddFT = null,
                marketNameHT = "HT Under 1.5",
                marketIdHT = "1.340551211",
                marketOddHT = "1.90",
                gameStatus = "HT",
                statusFT = null,
                statusHT = null,
                gameScore = "0-0"
            ),
            ChardrawDto(
                betfairId = 1111111113L,
                sofascoreId = 55503,
                eventName = "Sharks United vs Tigers",
                leagueName = "Serie A",
                homeName = "Sharks United",
                awayName = "Tigers",
                date = LocalDate.of(2025, 1, 15),
                hour = LocalTime.of(18, 0),
                marketNameFT = null,
                marketIdFT = null,
                marketOddFT = null,
                marketNameHT = "HT Over 0.5",
                marketIdHT = "1.998844221",
                marketOddHT = "1.60",
                gameStatus = "12'",
                statusFT = null,
                statusHT = null,
                gameScore = "0-0"
            ),
            ChardrawDto(
                betfairId = 1111111114L,
                sofascoreId = 55504,
                eventName = "Dragons FC vs Wolves",
                leagueName = "Bundesliga",
                homeName = "Dragons FC",
                awayName = "Wolves",
                date = LocalDate.of(2025, 1, 15),
                hour = LocalTime.of(20, 0),
                marketNameFT = null,
                marketIdFT = null,
                marketOddFT = null,
                marketNameHT = "HT Draw",
                marketIdHT = "1.551477332",
                marketOddHT = "2.10",
                gameStatus = "55'",
                statusFT = null,
                statusHT = null,
                gameScore = "1-1"
            ),
            ChardrawDto(
                betfairId = 1111111115L,
                sofascoreId = 55505,
                eventName = "Raptors vs Phoenix",
                leagueName = "Champions League",
                homeName = "Raptors",
                awayName = "Phoenix",
                date = LocalDate.of(2025, 1, 15),
                hour = LocalTime.of(21, 45),
                marketNameFT = null,
                marketIdFT = null,
                marketOddFT = null,
                marketNameHT = "HT BTTS",
                marketIdHT = "1.772661188",
                marketOddHT = "3.30",
                gameStatus = "23'",
                statusFT = null,
                statusHT = null,
                gameScore = "1-1"
            )
        )

        telegramNotifierHttpAdapter.sendMessageChardraw(mockGames)

    }

}