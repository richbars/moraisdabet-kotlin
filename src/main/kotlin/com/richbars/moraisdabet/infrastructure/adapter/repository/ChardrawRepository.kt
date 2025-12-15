package com.richbars.moraisdabet.infrastructure.adapter.repository

import com.richbars.moraisdabet.core.application.dto.ChardrawUpdate
import com.richbars.moraisdabet.infrastructure.repository.entity.ChardrawEntity
import com.richbars.moraisdabet.infrastructure.repository.entity.GoltrixEntity
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.query.Param

interface ChardrawRepository : CoroutineCrudRepository<ChardrawEntity, Long> {
    @Query("SELECT * FROM chardraw WHERE game_status NOT IN ('finished', 'postponed')")
    suspend fun getMatchsInProgress(): List<ChardrawEntity>

    @Modifying
    @Query("""
    UPDATE chardraw SET

        market_name_ft = COALESCE(market_name_ft, :#{#u.marketNameFT}),
        market_odd_ft  = COALESCE(market_odd_ft,  :#{#u.marketOddFT}),
        market_id_ft   = COALESCE(market_id_ft,   :#{#u.marketIdFT}),

        
        game_status = COALESCE(:#{#u.gameStatus}, game_status),
        status_ht = COALESCE(:#{#u.statusHT}, status_ht),
        status_ft = COALESCE(:#{#u.statusFT}, status_ft)
    WHERE betfair_id = :#{#u.betfairId}
""")
    suspend fun updateChardraw(@Param("u") u: ChardrawUpdate): Int

}

