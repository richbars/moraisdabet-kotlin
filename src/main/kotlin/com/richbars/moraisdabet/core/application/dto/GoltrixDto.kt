package com.richbars.moraisdabet.core.application.dto

import java.time.LocalDate

data class GoltrixDto(
    // Match Info
    val betfairId: Long,
    val sofascoreId: Long,
    val eventName: String,
    val leagueName: String,
    val homeName: String,
    val awayName: String,
    val date: LocalDate,

    //Market Back
    val marketName: String,
    val marketOdd: String,
    val marketId: String,

    //Alert Entry
    val alertName: String,
    val alertEntryMinute: Int?,
    val alertEntryScore: String,

    //Alert Exit
    val alertExitMinute: Int?,
    val alertExitScore: String?,

    //Match Status
    val gameStatus: String,
    val goltrixStatus: String?,
    val gameFinalScore: String
)