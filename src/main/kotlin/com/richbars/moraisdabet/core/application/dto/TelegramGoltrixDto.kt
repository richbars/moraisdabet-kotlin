package com.richbars.moraisdabet.core.application.dto

data class TelegramGoltrixDto(
    val alertName: String,
    val leagueName: String,
    val eventName: String,
    val alertEntryMinute: Int,
    val gameFinalScore: String,
    val odd: Double,
    val urlMarket: String
)