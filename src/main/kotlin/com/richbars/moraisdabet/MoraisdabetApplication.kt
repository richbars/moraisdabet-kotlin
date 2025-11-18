package com.richbars.moraisdabet

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class MoraisdabetApplication

@PostConstruct
@EventListener(ApplicationReadyEvent::class)
fun main(args: Array<String>) {
	runApplication<MoraisdabetApplication>(*args)
}