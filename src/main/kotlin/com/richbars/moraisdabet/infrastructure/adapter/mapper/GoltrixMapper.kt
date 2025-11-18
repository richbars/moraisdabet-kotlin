import com.richbars.moraisdabet.core.application.dto.GoltrixDto
import com.richbars.moraisdabet.core.application.dto.GoltrixUpdate
import com.richbars.moraisdabet.infrastructure.repository.entity.JpaGoltrixEntity

fun GoltrixDto.toEntity(): JpaGoltrixEntity =
    JpaGoltrixEntity(
        betfairId = this.betfairId,
        sofascoreId = this.sofascoreId,
        eventName = this.eventName,
        leagueName = this.leagueName,
        homeName = this.homeName,
        awayName = this.awayName,
        date = this.date, // Agora ambos são LocalDate? - sem conversão necessária
        alertMarketUnderName = this.alertMarketUnderName,
        alertOddUnder = this.alertOddUnder, // Agora ambos são Float? - sem conversão necessária
        alertMarketHtName = this.alertMarketHtName,
        alertOddHt = this.alertOddHt,       // Agora ambos são Float? - sem conversão necessária
        marketUnderId = this.marketUnderId, // Agora ambos são Double? - sem conversão necessária
        marketHtId = this.marketHtId,       // Agora ambos são Double? - sem conversão necessária
        alertName = this.alertName,
        alertEntryMinute = this.alertEntryMinute,  // Agora ambos são Int?
        alertEntryScore = this.alertEntryScore,
        alertExitMinute = this.alertExitMinute,
        alertExitScore = this.alertExitScore,
        gameStatus = this.gameStatus,
        goltrixStatus = this.goltrixStatus, // Agora ambos são String?
        gameFinalScore = this.gameFinalScore
    )

fun JpaGoltrixEntity.toModel(): GoltrixDto =
    GoltrixDto(
        betfairId = this.betfairId,
        sofascoreId = this.sofascoreId,
        eventName = this.eventName,
        leagueName = this.leagueName,
        homeName = this.homeName,
        awayName = this.awayName,
        date = this.date, // Agora ambos são LocalDate? - sem conversão necessária
        alertMarketUnderName = this.alertMarketUnderName,
        alertOddUnder = this.alertOddUnder, // Agora ambos são Float? - sem conversão necessária
        alertMarketHtName = this.alertMarketHtName,
        alertOddHt = this.alertOddHt,       // Agora ambos são Float? - sem conversão necessária
        marketUnderId = this.marketUnderId, // Agora ambos são Double? - sem conversão necessária
        marketHtId = this.marketHtId,       // Agora ambos são Double? - sem conversão necessária
        alertName = this.alertName,
        alertEntryMinute = this.alertEntryMinute,
        alertEntryScore = this.alertEntryScore,
        alertExitMinute = this.alertExitMinute,
        alertExitScore = this.alertExitScore,
        gameStatus = this.gameStatus,
        goltrixStatus = this.goltrixStatus,
        gameFinalScore = this.gameFinalScore
    )

fun JpaGoltrixEntity.toGoltrixUpdate(): GoltrixUpdate =
    GoltrixUpdate(
        betfairId = this.betfairId,
        alertName = this.alertName,
        alertExitMinute = this.alertExitMinute ?: 0,
        alertExitScore = this.alertExitScore ?: "",
        gameStatus = this.gameStatus,
        goltrixStatus = this.goltrixStatus ?: "",
        gameFinalScore = this.gameFinalScore
    )
