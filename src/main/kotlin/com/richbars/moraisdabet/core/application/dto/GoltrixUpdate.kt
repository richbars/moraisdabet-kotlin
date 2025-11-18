package com.richbars.moraisdabet.core.application.dto

data class GoltrixUpdate(
    val betfairId: Long,
    val alertName: String,
    val alertExitMinute: Int?,
    val alertExitScore: String,
    val gameStatus: String,
    val goltrixStatus: String,
    val gameFinalScore: String
)
