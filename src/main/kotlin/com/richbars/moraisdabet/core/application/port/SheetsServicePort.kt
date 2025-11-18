package com.richbars.moraisdabet.core.application.port
import com.richbars.moraisdabet.core.application.dto.GoltrixDto
import com.richbars.moraisdabet.core.application.dto.GoltrixUpdate

interface SheetsServicePort {
    suspend fun createGoltrixRow(goltrixDto: GoltrixDto): GoltrixDto
    suspend fun updateGoltrixRow(goltrixUpdate: GoltrixUpdate): GoltrixUpdate
}