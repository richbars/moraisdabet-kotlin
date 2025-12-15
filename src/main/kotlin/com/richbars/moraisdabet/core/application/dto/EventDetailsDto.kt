package com.richbars.moraisdabet.core.application.dto

data class EventDetailsDto(
    val eventId: Long,
    val eventName: String,
    val competitionName: String,
    val competitionId: Long,
    val startTime: String,
    val homeName: String,
    val awayName: String,
    val gameStatus: String
)