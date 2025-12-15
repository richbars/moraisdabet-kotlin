package com.richbars.moraisdabet

import com.richbars.moraisdabet.core.application.dto.ChardrawUpdate
import com.richbars.moraisdabet.infrastructure.adapter.out.BetfairHttpAdapter
import com.richbars.moraisdabet.infrastructure.adapter.out.FulltraderHttpAdapter
import com.richbars.moraisdabet.infrastructure.adapter.out.SheetsServiceAdapter
import com.richbars.moraisdabet.infrastructure.http.HttpClientManager
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    classes = [
        SheetsServiceAdapter::class,
    ],
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration"
    ]
)
class SheetsServiceTest {

    @Autowired
    lateinit var sheetsServiceAutowired: SheetsServiceAdapter

    @Test
    fun `should update row in sheets`() = runTest {

        val updated = ChardrawUpdate(
            betfairId = 34949377,
            marketNameFT = "Over/Under 0.5",
            marketOddFT = "2.20",
            marketIdFT = "1.151548",
            gameStatus = "Testado Richard",
            statusFT = "WINNER",
            statusHT = "LOSER"
        )

        sheetsServiceAutowired.updateChardrawRow(updated)

    }

}