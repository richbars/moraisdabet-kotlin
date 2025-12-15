package com.richbars.moraisdabet.infrastructure.repository.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate

@Table("goltrix")
data class GoltrixEntity(

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

    @Column("market_name")
    val marketName: String,

    @Column("market_odd")
    val marketOdd: String,

    @Column("market_id")
    val marketId: String,

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