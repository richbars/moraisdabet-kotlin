package com.richbars.moraisdabet.infrastructure.repository.mapper

import com.richbars.moraisdabet.core.application.dto.ChardrawDto
import com.richbars.moraisdabet.infrastructure.repository.entity.ChardrawEntity

object ChardrawMapper {

    fun toModel(entity: ChardrawEntity): ChardrawDto =
        ChardrawDto(
            betfairId = entity.betfairId,
            sofascoreId = entity.sofascoreId,
            eventName = entity.eventName,
            leagueName = entity.leagueName,
            homeName = entity.homeName,
            awayName = entity.awayName,
            date = entity.date,
            hour = entity.hour,
            marketNameHT = entity.marketNameHT,
            marketOddHT = entity.marketOddHT,
            marketIdHT = entity.marketIdHT,
            marketNameFT = entity.marketNameFT,
            marketOddFT = entity.marketOddFT,
            marketIdFT = entity.marketIdFT,
            gameStatus = entity.gameStatus,
            statusHT = entity.statusHT,
            statusFT = entity.statusFT,
            gameScore = entity.gameScore
        )

    fun toEntity(model: ChardrawDto): ChardrawEntity =
        ChardrawEntity(
            betfairId = model.betfairId,
            sofascoreId = model.sofascoreId,
            eventName = model.eventName,
            leagueName = model.leagueName,
            homeName = model.homeName,
            awayName = model.awayName,
            date = model.date,
            hour = model.hour,
            marketNameHT = model.marketNameHT,
            marketOddHT = model.marketOddHT,
            marketIdHT = model.marketIdHT,
            marketNameFT = model.marketNameFT,
            marketOddFT = model.marketOddFT,
            marketIdFT = model.marketIdFT,
            gameStatus = model.gameStatus,
            statusHT = model.statusHT,
            statusFT = model.statusFT,
            gameScore = model.gameScore
        )
}
