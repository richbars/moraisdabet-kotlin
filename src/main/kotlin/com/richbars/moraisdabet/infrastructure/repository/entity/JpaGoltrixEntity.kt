package com.richbars.moraisdabet.infrastructure.repository.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigInteger
import java.time.LocalDate
import kotlin.random.Random

@Table("goltrix")
data class JpaGoltrixEntity(

    @Column("betfair_id")
    val betfairId: Long,

    @Column("sofascore_id")
    val sofascoreId: Long,

    @Column("event_name")
    val eventName: String,

    @Column("league_name")
    val leagueName: String,

    @Column("home_name")
    val homeName: String,

    @Column("away_name")
    val awayName: String,

    @Column("date")
    val date: LocalDate,

    @Column("alert_market_under_name")
    val alertMarketUnderName: String? = null,

    @Column("alert_odd_under")
    val alertOddUnder: Double? = null,

    @Column("alert_market_ht_name")
    val alertMarketHtName: String? = null,

    @Column("alert_odd_ht")
    val alertOddHt: Double? = null,

    @Column("market_under_id")
    val marketUnderId: Double? = null,

    @Column("market_ht_id")
    val marketHtId: Double? = null,

    @Column("alert_name")
    val alertName: String,

    @Column("alert_entry_minute")
    val alertEntryMinute: Int?,

    @Column("alert_entry_score")
    val alertEntryScore: String,

    @Column("alert_exit_minute")
    val alertExitMinute: Int? = null,

    @Column("alert_exit_score")
    val alertExitScore: String? = null,

    @Column("game_status")
    val gameStatus: String,

    @Column("goltrix_status")
    val goltrixStatus: String?,

    @Column("game_final_score")
    val gameFinalScore: String
)