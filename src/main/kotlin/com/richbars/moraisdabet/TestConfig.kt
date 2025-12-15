package com.richbars.moraisdabet

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.PropertySources

@Configuration
@ComponentScan(
    basePackages = [
        // Infraestrutura HTTP
        "com.richbars.moraisdabet.infrastructure.http",

        // Adapters externos (Fulltrader, Betfair, Sofascore, Telegram, etc.)
        "com.richbars.moraisdabet.infrastructure.adapter.out",

        // Implementações de PORTS (como GoltrixImplRepository)
//        "com.richbars.moraisdabet.infrastructure.adapter.impl",

        // Repositórios do domínio
//        "com.richbars.moraisdabet.infrastructure.repository",

        // Serviços da camada application
        "com.richbars.moraisdabet.core.application.service"
    ]
)
@PropertySources(
    value = [
        PropertySource(
            value = ["classpath:application-test.yml"],
            ignoreResourceNotFound = true
        ),
        PropertySource(
            value = ["file:.env"],
            ignoreResourceNotFound = true
        )
    ]
)
class TestConfig
