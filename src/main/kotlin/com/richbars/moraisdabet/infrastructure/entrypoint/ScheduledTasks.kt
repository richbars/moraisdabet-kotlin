package com.richbars.moraisdabet.infrastructure.entrypoint

import com.richbars.moraisdabet.core.application.port.GoltrixPort
import com.richbars.moraisdabet.core.application.service.GoltrixService
import com.richbars.moraisdabet.infrastructure.adapter.repository.JpaGoltrixRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScheduledTasks(
    private val goltrixService: GoltrixService
) {

    private val log = LoggerFactory.getLogger(GoltrixService::class.java)
    private val executeMutex = Mutex()

    // Executa a cada 1 minuto
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
