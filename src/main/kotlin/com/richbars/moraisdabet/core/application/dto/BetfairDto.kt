package com.richbars.moraisdabet.core.application.dto

import java.time.LocalDate
import java.time.LocalTime

data class EventBetfairDto(
    val eventId: Long,
    val eventName: String,
    val league: String,
    val home: String,
    val away: String,
    val date: LocalDate,
    val hour: LocalTime,
)

data class MarketBetfairDto(
    val lay: Lay?,
    val back: Back?
)

data class Lay(
    val marketName: String,
    val marketId: Double,
    val marketOdd: Double
)

data class Back(
    val marketName: String,
    val marketId: Double,
    val marketOdd: Double
)
