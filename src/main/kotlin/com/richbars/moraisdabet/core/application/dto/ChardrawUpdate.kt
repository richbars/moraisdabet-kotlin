package com.richbars.moraisdabet.core.application.dto

data class ChardrawUpdate(
    val betfairId: Long,

    val marketNameFT: String?,
    val marketOddFT: String?,
    val marketIdFT: String?,

    val gameStatus: String?,
    val statusHT: String?,
    val statusFT: String?,
)
