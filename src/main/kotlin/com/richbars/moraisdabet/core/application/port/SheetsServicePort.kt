package com.richbars.moraisdabet.core.application.port
import com.richbars.moraisdabet.core.application.dto.ChardrawDto
import com.richbars.moraisdabet.core.application.dto.ChardrawUpdate
import com.richbars.moraisdabet.core.application.dto.GoltrixDto
import com.richbars.moraisdabet.core.application.dto.GoltrixUpdate

interface SheetsServicePort {
    suspend fun createGoltrixRow(goltrixDto: GoltrixDto): GoltrixDto
    suspend fun updateGoltrixRow(goltrixUpdate: GoltrixUpdate): GoltrixUpdate
    suspend fun createChardrawRow(chardrawDto: ChardrawDto): ChardrawDto
    suspend fun updateChardrawRow(chardrawUpdate: ChardrawUpdate)
}