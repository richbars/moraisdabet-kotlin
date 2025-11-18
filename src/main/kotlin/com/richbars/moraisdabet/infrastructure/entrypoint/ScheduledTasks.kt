package com.richbars.moraisdabet.infrastructure.entrypoint

import com.richbars.moraisdabet.core.application.dto.GoltrixDto
import com.richbars.moraisdabet.core.application.port.GoltrixPort
import com.richbars.moraisdabet.core.application.service.GoltrixService
import com.richbars.moraisdabet.infrastructure.adapter.repository.GoltrixRepositoryImpl
import com.richbars.moraisdabet.infrastructure.adapter.repository.JpaGoltrixRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import toEntity
import java.time.LocalDate

@Component
class ScheduledTasks(
    private val goltrixService: GoltrixService,
    private val goltrixPort: GoltrixPort,
    private val jpaGoltrixRepository: JpaGoltrixRepository
) {

    private val log = LoggerFactory.getLogger(GoltrixService::class.java)
    private val executeMutex = Mutex()

    // Executa a cada 1 minuto
    @Scheduled(fixedDelay = 60_000)
    fun runExecuteEvery1m() {
        kotlinx.coroutines.GlobalScope.launch {
            executeMutex.withLock {
                log.debug("Starting execute process")
                goltrixService.execute()
                goltrixService.update()
                log.debug("Finished execute process")
            }
        }
    }

//    @Scheduled(fixedDelay = 60_000)
//    suspend fun runExecuteEvery1m() {
//
//        val dto = GoltrixDto(
//            betfairId = 34953901,
//            sofascoreId = 13233692,
//            eventName = "TEEEEEEEEEEEEEEESTE",
//            leagueName = "FIFA World Cup Qualifiers - Europe",
//            homeName = "Spain",
//            awayName = "Türkiye",
//            date = LocalDate.of(2025, 11, 18),
//            alertMarketUnderName = "t",
//            alertOddUnder = 1.16,
//            alertMarketHtName = "ttt",
//            alertOddHt = 1.15,
//            marketUnderId = 1.26,
//            marketHtId = 1.36,
//            alertName = "Com Teste",
//            alertEntryMinute = 95,
//            alertEntryScore = "2-2",
//            alertExitMinute = 55,
//            alertExitScore = "b",
//            gameStatus = "inprogress",
//            goltrixStatus = "c",
//            gameFinalScore = "2-2"
//        )
//
//        val t = dto.toEntity()
//
//        try {
//            val result = jpaGoltrixRepository.save(t)
//            println(result)
//        } catch (ex: Exception){
//            println(ex)
//        }
//
//    }

}
