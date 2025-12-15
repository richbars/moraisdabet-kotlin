package com.richbars.moraisdabet.infrastructure.repository.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalTime

@Table("chardraw")
data class ChardrawEntity(
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

    @Column("hour")
    val hour: LocalTime,

    @Column("market_name_ht")
    val marketNameHT: String?,

    @Column("market_odd_ht")
    val marketOddHT: String?,

    @Column("market_id_ht")
    val marketIdHT: String?,

    @Column("market_name_ft")
    val marketNameFT: String?,

    @Column("market_odd_ft")
    val marketOddFT: String?,

    @Column("market_id_ft")
    val marketIdFT: String?,

    @Column("game_status")
    val gameStatus: String?,

    @Column("status_ht")
    val statusHT: String?,

    @Column("status_ft")
    val statusFT: String?,
)
