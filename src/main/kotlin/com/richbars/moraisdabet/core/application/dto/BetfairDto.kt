package com.richbars.moraisdabet.core.application.dto

import com.richbars.moraisdabet.core.application.port.MarketBasePort
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

data class MarketBetfairPeriodDto(
    val halftime: Halftime?,
    val fulltime: Fulltime?
)

data class Halftime(
    val marketName: String,
    val marketId: String,
    val marketOdd: String
)

data class Fulltime(
    val marketName: String,
    val marketId: String,
    val marketOdd: String
)

data class Lay(
    override val marketName: String,
    override val marketId: String,
    override val marketOdd: String
) : MarketBasePort

data class Back(
    override val marketName: String,
    override val marketId: String,
    override val marketOdd: String
) : MarketBasePort
