package com.richbars.moraisdabet.core.application.port
import com.richbars.moraisdabet.core.application.dto.EventBetfairDto
import com.richbars.moraisdabet.core.application.dto.EventDetailsDto
import com.richbars.moraisdabet.core.application.dto.MarketBetfairDto
import com.richbars.moraisdabet.core.application.dto.MarketBetfairPeriodDto

interface BetfairHttpPort {
    suspend fun getEventById(eventId: Long): EventBetfairDto
    suspend fun getMarketById(eventId: Long, alertName: String): MarketBetfairDto?
    suspend fun getMarketByIdGoltrix(eventId: Long, alertName: String): MarketBetfairDto?
    suspend fun getStatusMarketById(marketId: String): String
    suspend fun getMarketByIdChardraw(eventId: Long): MarketBetfairPeriodDto
    suspend fun getEventDetailsById(eventId: Long): EventDetailsDto

}