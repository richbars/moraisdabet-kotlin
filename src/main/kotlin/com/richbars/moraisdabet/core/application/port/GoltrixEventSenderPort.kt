package com.richbars.moraisdabet.core.application.port

import com.richbars.moraisdabet.core.application.dto.GoltrixDto
import com.richbars.moraisdabet.core.application.dto.GoltrixUpdate

interface GoltrixEventSenderPort {
    suspend fun send(goltrixDto: GoltrixDto)
    suspend fun sendUpdate(goltrixUpdate: GoltrixUpdate)
}