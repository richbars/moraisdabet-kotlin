package com.richbars.moraisdabet

import com.richbars.moraisdabet.core.application.dto.GoltrixDto
import com.richbars.moraisdabet.infrastructure.adapter.messaging.sender.GoltrixEventSender
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.event.EventListener
import java.time.LocalDate

@SpringBootTest
class GoltrixEventSenderTest {

    @Autowired
    lateinit var goltrixEventSender: GoltrixEventSender

    @Test
    fun `should send GoltrixDto to SQS`() = runBlocking {
        // Cria DTO de teste
        val goltrixDto = GoltrixDto(
            betfairId = 123456L,
            sofascoreId = 654321L,
            eventName = "Time A vs Time B",
            leagueName = "Premier League",
            homeName = "Time A",
            awayName = "Time B",
            date = LocalDate.now(),
            alertMarketUnderName = "Under 2.5",
            alertOddUnder = 1.8,
            alertMarketHtName = "HT Over 1.0",
            alertOddHt = 1.5,
            marketUnderId = 101.0,
            marketHtId = 202.0,
            alertName = "Entrada Under",
            alertEntryMinute = 23,
            alertEntryScore = "0-0",
            alertExitMinute = 45,
            alertExitScore = "1-0",
            gameStatus = "FT",
            goltrixStatus = "OK",
            gameFinalScore = "1-0"
        )

        // Chama o envio para SQS
        goltrixEventSender.send(goltrixDto)
    }
}