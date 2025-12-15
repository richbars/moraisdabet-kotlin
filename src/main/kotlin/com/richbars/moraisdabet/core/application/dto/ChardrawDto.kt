package com.richbars.moraisdabet.core.application.dto

import java.time.LocalDate
import java.time.LocalTime

data class ChardrawDto(
    val betfairId: Long,
    val sofascoreId: Long,
    val eventName: String,
    val leagueName: String,
    val homeName: String,
    val awayName: String,
    val date: LocalDate,
    val hour: LocalTime,

    val marketNameHT: String?,
    val marketOddHT: String?,
    val marketIdHT: String?,

    val marketNameFT: String?,
    val marketOddFT: String?,
    val marketIdFT: String?,

    val gameStatus: String?,
    val statusHT: String?,
    val statusFT: String?,
)