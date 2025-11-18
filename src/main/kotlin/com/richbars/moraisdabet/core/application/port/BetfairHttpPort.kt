package com.richbars.moraisdabet.core.application.port
import com.richbars.moraisdabet.core.application.dto.EventBetfairDto
import com.richbars.moraisdabet.core.application.dto.MarketBetfairDto

interface BetfairHttpPort {
    suspend fun getEventById(eventId: Long): EventBetfairDto
    suspend fun getMarketById(eventId: Long, alertName: String): MarketBetfairDto?
    suspend fun getStatusMarketById(marketId: Double): String
}