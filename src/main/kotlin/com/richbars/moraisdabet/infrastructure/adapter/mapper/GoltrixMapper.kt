package com.richbars.moraisdabet.infrastructure.adapter.mapper
import com.richbars.moraisdabet.core.application.dto.GoltrixDto
import com.richbars.moraisdabet.infrastructure.repository.entity.GoltrixEntity

fun GoltrixDto.toEntity(): GoltrixEntity =
    GoltrixEntity(
        betfairId = this.betfairId,
        sofascoreId = this.sofascoreId,
        eventName = this.eventName,
        leagueName = this.leagueName,
        homeName = this.homeName,
        awayName = this.awayName,
        date = this.date,
        marketName = this.marketName,
        marketOdd = this.marketOdd,
        marketId = this.marketId,
        alertName = this.alertName,
        alertEntryMinute = this.alertEntryMinute,
        alertEntryScore = this.alertEntryScore,
        alertExitMinute = this.alertExitMinute,
        alertExitScore = this.alertExitScore,
        gameStatus = this.gameStatus,
        goltrixStatus = this.goltrixStatus,
        gameFinalScore = this.gameFinalScore
    )


fun GoltrixEntity.toModel(): GoltrixDto =
    GoltrixDto(
        betfairId = this.betfairId,
        sofascoreId = this.sofascoreId,
        eventName = this.eventName,
        leagueName = this.leagueName,
        homeName = this.homeName,
        awayName = this.awayName,
        date = this.date,
        marketName = this.marketName,
        marketOdd = this.marketOdd,
        marketId = this.marketId,
        alertName = this.alertName,
        alertEntryMinute = this.alertEntryMinute,
        alertEntryScore = this.alertEntryScore,
        alertExitMinute = this.alertExitMinute,
        alertExitScore = this.alertExitScore,
        gameStatus = this.gameStatus,
        goltrixStatus = this.goltrixStatus,
        gameFinalScore = this.gameFinalScore
    )
