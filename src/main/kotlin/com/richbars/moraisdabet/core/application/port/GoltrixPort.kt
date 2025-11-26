package com.richbars.moraisdabet.core.application.port

import com.richbars.moraisdabet.core.application.dto.GoltrixDto
import com.richbars.moraisdabet.core.application.dto.GoltrixUpdate

interface GoltrixPort {
    suspend fun save(goltrix: GoltrixDto): Boolean
    suspend fun findByBetfairIdAndAlertName(betfairId: Long, alertName: String): GoltrixDto?
    suspend fun getMatchsInProgress(): List<GoltrixDto>
    suspend fun deleteByBetfairId(betfairId: Long, alertName: String)
    suspend fun findAll(): List<GoltrixDto>
    suspend fun updateGoltrix(goltrix: GoltrixUpdate): Boolean
    suspend fun verifyExit(): List<GoltrixDto>
}