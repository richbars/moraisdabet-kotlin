CREATE TABLE IF NOT EXISTS goltrix (

    betfair_id BIGINT NOT NULL,
    sofascore_id BIGINT NOT NULL,
    event_name VARCHAR(255) NOT NULL,
    league_name VARCHAR(255) NOT NULL,
    home_name VARCHAR(255) NOT NULL,
    away_name VARCHAR(255) NOT NULL,
    date DATE,
    alert_market_under_name VARCHAR(255),
    alert_odd_under VARCHAR(255),
    alert_market_ht_name VARCHAR(255),
    alert_odd_ht VARCHAR(255),
    market_under_id VARCHAR(255),
    market_ht_id VARCHAR(255),
    alert_name VARCHAR(255) NOT NULL,
    alert_entry_minute INTEGER,
    alert_entry_score VARCHAR(50) NOT NULL,
    alert_exit_minute VARCHAR(50),
    alert_exit_score VARCHAR(50),
    game_status VARCHAR(50) NOT NULL,
    goltrix_status VARCHAR(50),
    game_final_score VARCHAR(50) NOT NULL,

    -- ⭐ AQUI O UNIQUE QUE VOCÊ PRECISA
    CONSTRAINT unique_betfair_alert UNIQUE (betfair_id, alert_name)
);
