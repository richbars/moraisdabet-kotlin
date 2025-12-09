package com.richbars.moraisdabet.infrastructure.entrypoint

import com.richbars.moraisdabet.core.application.service.ChardrawService
import com.richbars.moraisdabet.core.application.service.GoltrixService
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScheduledTasks(
    private val goltrixService: GoltrixService,
    private val chardrawService: ChardrawService
) {

    private val executeMutex = Mutex()

    @Scheduled(cron = "0 34 0 * * *", zone = "America/Sao_Paulo")
    fun runChardrawDaily() {
        runBlocking {
            executeMutex.withLock {
                chardrawService.saveGames()
            }
        }
    }

    @Scheduled(fixedDelay = 60_000)
    fun runExecuteEvery1m() {
        runBlocking {
            executeMutex.withLock {
                goltrixService.execute()
                goltrixService.update()
            }
        }
    }
}