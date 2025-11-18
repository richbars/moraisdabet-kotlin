package com.richbars.moraisdabet.infrastructure.adapter.messaging.config

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest
import software.amazon.awssdk.services.sqs.model.QueueAttributeName

@Component
class SqsAutoCreateQueue(
    private val sqsAsyncClient: SqsAsyncClient,
    @Value("\${spring.aws.sqs.queue-name.sheets-processing}") private val queueName: String
) {

    private val log = LoggerFactory.getLogger(SqsAutoCreateQueue::class.java)

    @PostConstruct
    fun init() {
        log.info("SQS Listener escutando fila: ${queueName}")
    }

    @PostConstruct
    fun createQueue() {
        log.info("Checking existence of SQS FIFO queue: $queueName")

        val attributes = mapOf(
            QueueAttributeName.FIFO_QUEUE to "true",
            QueueAttributeName.CONTENT_BASED_DEDUPLICATION to "true"
        )

        val request = CreateQueueRequest.builder()
            .queueName(queueName) // precisa terminar com .fifo
            .attributes(attributes)
            .build()

        sqsAsyncClient.createQueue(request)
            .whenComplete { _, error ->
                if (error != null) {
                    log.warn("Queue already exists or error creating it: ${error.message}")
                } else {
                    log.info("FIFO Queue successfully created: $queueName")
                }
            }
    }
}
