package com.richbars.moraisdabet.core.application.dto

data class TelegramGoltrixDto(
    val alertName: String,
    val leagueName: String,
    val eventName: String,
    val homeName: String,
    val awayName: String,
    val alertEntryMinute: Int,
    val gameFinalScore: String,
    val odd: String,
    val urlMarket: String,
    val urlGame: String
)