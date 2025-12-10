CREATE TABLE IF NOT EXISTS goltrix (
    betfair_id BIGINT NOT NULL,
    sofascore_id BIGINT NOT NULL,
    event_name VARCHAR(255) NOT NULL,
    league_name VARCHAR(255) NOT NULL,
    home_name VARCHAR(255) NOT NULL,
    away_name VARCHAR(255) NOT NULL,
    date DATE,
    market_name VARCHAR(255),
    market_odd VARCHAR(255),
    market_id VARCHAR(255),
    alert_name VARCHAR(255) NOT NULL,
    alert_entry_minute INTEGER,
    alert_entry_score VARCHAR(50) NOT NULL,
    alert_exit_minute VARCHAR(50),
    alert_exit_score VARCHAR(50),
    game_status VARCHAR(50) NOT NULL,
    goltrix_status VARCHAR(50),
    game_final_score VARCHAR(50) NOT NULL,

    CONSTRAINT unique_betfair_alert UNIQUE (betfair_id, alert_name)
);

CREATE TABLE IF NOT EXISTS chardraw (
    betfair_id BIGINT NOT NULL,
    sofascore_id BIGINT NOT NULL,
    event_name VARCHAR(255) NOT NULL,
    league_name VARCHAR(255) NOT NULL,
    home_name VARCHAR(255) NOT NULL,
    away_name VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    hour TIME NOT NULL,

    market_name_ht VARCHAR(255) NOT NULL,
    market_odd_ht VARCHAR(50) NOT NULL,
    market_id_ht VARCHAR(100) NOT NULL,

    market_name_ft VARCHAR(255),
    market_odd_ft VARCHAR(50),
    market_id_ft VARCHAR(100),

    game_status VARCHAR(50),
    status_ht VARCHAR(50),
    status_ft VARCHAR(50),

    PRIMARY KEY (betfair_id)
);
