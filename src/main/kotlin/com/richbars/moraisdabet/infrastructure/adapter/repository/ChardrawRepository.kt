package com.richbars.moraisdabet.infrastructure.adapter.repository

import com.richbars.moraisdabet.core.application.dto.ChardrawUpdate
import com.richbars.moraisdabet.infrastructure.repository.entity.ChardrawEntity
import com.richbars.moraisdabet.infrastructure.repository.entity.GoltrixEntity
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.query.Param

interface ChardrawRepository : CoroutineCrudRepository<ChardrawEntity, Long> {
    @Query("SELECT * FROM chardraw WHERE game_status != 'finished'")
    suspend fun getMatchsInProgress(): List<ChardrawEntity>

    @Modifying
    @Query("""
    UPDATE chardraw SET
        market_name_ft = COALESCE(:#{#u.marketNameFT}, market_name_ft),
        market_odd_ft = COALESCE(:#{#u.marketOddFT}, market_odd_ft),
        market_id_ft = COALESCE(:#{#u.marketIdFT}, market_id_ft),
        game_status = COALESCE(:#{#u.gameStatus}, game_status),
        status_ht = COALESCE(:#{#u.statusHT}, status_ht),
        status_ft = COALESCE(:#{#u.statusFT}, status_ft),
        game_score = COALESCE(:#{#u.gameScore}, game_score)
    WHERE betfair_id = :#{#u.betfairId}
""")
    suspend fun updateChardraw(@Param("u") u: ChardrawUpdate): Int

}

