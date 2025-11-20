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

    //Alert Lay
    val alertMarketUnderName: String?,
    val alertOddUnder: String?,

    //Alert Back
    val alertMarketHtName: String?,
    val alertOddHt: String?,

    //Market Id
    val marketUnderId: String?,
    val marketHtId: String?,

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