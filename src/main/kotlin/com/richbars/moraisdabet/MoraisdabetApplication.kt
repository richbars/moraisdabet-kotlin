package com.richbars.moraisdabet

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class MoraisdabetApplication

fun main(args: Array<String>) {
	runApplication<MoraisdabetApplication>(*args)
}
