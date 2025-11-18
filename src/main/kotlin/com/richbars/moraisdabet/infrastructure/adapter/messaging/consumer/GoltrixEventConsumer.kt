//package com.richbars.moraisdabet.infrastructure.adapter.messaging.consumer
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.richbars.moraisdabet.core.application.dto.GoltrixDto
//import com.richbars.moraisdabet.core.application.port.SheetsServicePort
//import kotlinx.coroutines.DelicateCoroutinesApi
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.future.await
//import kotlinx.coroutines.launch
//import org.slf4j.LoggerFactory
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.boot.context.event.ApplicationReadyEvent
//import org.springframework.context.event.EventListener
//import org.springframework.stereotype.Component
//import software.amazon.awssdk.services.sqs.SqsAsyncClient
//import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
//import software.amazon.awssdk.services.sqs.model.Message
//import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
//
//@Component
//class GoltrixEventConsumer(
//    private val sqsAsyncClient: SqsAsyncClient,
//    private val objectMapper: ObjectMapper,
//    private val sheetsServicePort: SheetsServicePort,
//    @Value("\${spring.aws.sqs.queue-name.sheets-processing}") private val queueName: String
//) {
//
//    private val log = LoggerFactory.getLogger(GoltrixEventConsumer::class.java)
//
//    private suspend fun getQueueUrl(): String {
//        val response = sqsAsyncClient
//            .getQueueUrl { it.queueName(queueName) }
//            .await()
//
//        return response.queueUrl()
//    }
//
//    @OptIn(DelicateCoroutinesApi::class)
//    @EventListener(ApplicationReadyEvent::class)
//    fun startConsumer() {
//        GlobalScope.launch {
//            processMessages()
//        }
//    }
//
//    private suspend fun processMessages() {
//        val queueUrl = getQueueUrl()
//
//        log.info("📭 Started listening to queue: $queueUrl")
//
//        while (true) {
//            try {
//                val receiveRequest = ReceiveMessageRequest.builder()
//                    .queueUrl(queueUrl)
//                    .maxNumberOfMessages(10)
//                    .waitTimeSeconds(20)
//                    .build()
//
//                val messages = sqsAsyncClient.receiveMessage(receiveRequest).await().messages()
//
//                if (messages.isNotEmpty()) {
//                    log.info("📥 Received ${messages.size} messages from queue")
//
//                    messages.forEach { message ->
//                        processMessage(message, queueUrl)
//                    }
//                }
//            } catch (ex: Exception) {
//                log.error("❌ Error processing messages from SQS", ex)
//                kotlinx.coroutines.delay(5000)
//            }
//        }
//    }
//
//    private suspend fun processMessage(message: Message, queueUrl: String) {
//        try {
//            val goltrixDto = objectMapper.readValue(message.body(), GoltrixDto::class.java)
//            log.info("🔄 Processing message with ID: ${message.messageId()}")
//
//            // Usa o SheetsServicePort para criar a linha
//            val createdGoltrix = sheetsServicePort.createGoltrixRow(goltrixDto)
//            log.info("✅ Successfully created Goltrix row for event: ${createdGoltrix.eventName}")
//
//            // Remove a mensagem da fila após processamento bem-sucedido
//            deleteMessage(message, queueUrl)
//
//        } catch (ex: Exception) {
//            log.error("❌ Error processing message: ${message.body()}", ex)
//        }
//    }
//
//    private suspend fun deleteMessage(message: Message, queueUrl: String) {
//        try {
//            val deleteRequest = DeleteMessageRequest.builder()
//                .queueUrl(queueUrl)
//                .receiptHandle(message.receiptHandle())
//                .build()
//
//            sqsAsyncClient.deleteMessage(deleteRequest).await()
//            log.debug("🗑️ Message deleted successfully: ${message.messageId()}")
//
//        } catch (ex: Exception) {
//            log.error("❌ Error deleting message: ${message.messageId()}", ex)
//        }
//    }
//}