package com.richbars.moraisdabet.infrastructure.adapter.messaging.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.richbars.moraisdabet.core.application.dto.GoltrixDto
import com.richbars.moraisdabet.core.application.dto.GoltrixUpdate
import com.richbars.moraisdabet.core.application.port.SheetsServicePort
import io.awspring.cloud.sqs.annotation.SqsListener
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.model.Message

@Component
class SheetsEventListenerAdapter(
    private val sheetsServicePort: SheetsServicePort
) {

    private val logger = LoggerFactory.getLogger(SheetsEventListenerAdapter::class.java)

    private val objectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerModule(com.fasterxml.jackson.module.kotlin.KotlinModule.Builder().build())

    @SqsListener("\${spring.aws.sqs.queue-name.sheets-processing}")
    fun onMessage(message: Message) = runBlocking {
        try {
            val operation = message.messageAttributes()["operation"]?.stringValue()

            if (operation == null) {
                logger.error("Mensagem recebida sem 'operation' no atributo. Body: ${message.body()}")
                return@runBlocking
            }

            logger.info("[SheetsEventListener] Mensagem recebida com operação '$operation' → ${message.body()}")

            when (operation) {

                "CREATE" -> {
                    val dto = objectMapper.readValue(message.body(), GoltrixDto::class.java)
                    sheetsServicePort.createGoltrixRow(dto)
                }

                "UPDATE" -> {
                    val dto = objectMapper.readValue(message.body(), GoltrixUpdate::class.java)
                    sheetsServicePort.updateGoltrixRow(dto)
                }

                else -> {
                    logger.warn("[SheetsEventListener] Operação desconhecida: $operation")
                }
            }

        } catch (ex: Exception) {
            logger.error("[SheetsEventListener] Erro ao processar mensagem do SQS: ${message.body()}", ex)
        }
    }
}
