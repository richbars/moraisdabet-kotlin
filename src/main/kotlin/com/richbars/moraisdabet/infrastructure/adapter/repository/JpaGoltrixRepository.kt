package com.richbars.moraisdabet.infrastructure.adapter.repository

import com.richbars.moraisdabet.core.application.dto.GoltrixDto
import com.richbars.moraisdabet.infrastructure.repository.entity.JpaGoltrixEntity
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface JpaGoltrixRepository : CoroutineCrudRepository<JpaGoltrixEntity, Long> {

    @Query("""
    INSERT INTO goltrix(
        betfair_id, sofascore_id, event_name, league_name, home_name, away_name, date,
        alert_market_under_name, alert_odd_under, alert_market_ht_name, alert_odd_ht,
        market_under_id, market_ht_id, alert_name, alert_entry_minute, alert_entry_score,
        alert_exit_minute, alert_exit_score, game_status, goltrix_status, game_final_score
    )
    VALUES (
        :betfairId, :sofascoreId, :eventName, :leagueName, :homeName, :awayName, :date,
        :alertMarketUnderName, :alertOddUnder, :alertMarketHtName, :alertOddHt,
        :marketUnderId, :marketHtId, :alertName, :alertEntryMinute, :alertEntryScore,
        :alertExitMinute, :alertExitScore, :gameStatus, :goltrixStatus, :gameFinalScore
    )
    ON CONFLICT (betfair_id, alert_name) DO NOTHING
""")
    suspend fun insertWithConflict(entity: JpaGoltrixEntity)

    @Query("SELECT * FROM goltrix WHERE alert_exit_minute IS NULL")
    suspend fun verifyExit(): List<JpaGoltrixEntity>

    @Query("SELECT * FROM goltrix WHERE betfair_id = :betfairId AND alert_name = :alertName")
    suspend fun findByBetfairIdAndAlertName(betfairId: Long, alertName: String): JpaGoltrixEntity?

    @Query("SELECT * FROM goltrix WHERE game_status != 'finished'")
    suspend fun getMatchsInProgress(): List<JpaGoltrixEntity>

    @Query("SELECT * FROM goltrix")
    suspend fun getAll(): List<JpaGoltrixEntity>

    @Query("DELETE FROM goltrix WHERE betfair_id = :betfairId AND alert_name = :alertName")
    suspend fun deleteByBetfairId(betfairId: Long, alertName: String)

    @Modifying
    @Query(
        """
        UPDATE goltrix
        SET 
            alert_exit_minute = :alertExitMinute,
            alert_exit_score = :alertExitScore,
            game_status = :gameStatus,
            goltrix_status = :goltrixStatus,
            game_final_score = :gameFinalScore
        WHERE betfair_id = :betfairId
          AND alert_name = :alertName
        """
    )
    suspend fun updateGoltrix(
        betfairId: Long,
        alertName: String,
        alertExitMinute: Int?,
        alertExitScore: String,
        gameStatus: String,
        goltrixStatus: String,
        gameFinalScore: String
    ): Int
}

