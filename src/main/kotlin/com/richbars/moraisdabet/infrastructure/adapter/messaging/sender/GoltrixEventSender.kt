package com.richbars.moraisdabet.infrastructure.adapter.messaging.sender

import com.fasterxml.jackson.databind.ObjectMapper
import com.richbars.moraisdabet.core.application.dto.GoltrixDto
import com.richbars.moraisdabet.core.application.dto.GoltrixUpdate
import com.richbars.moraisdabet.core.application.port.GoltrixEventSenderPort
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

@Component
class GoltrixEventSender(
    private val sqsAsyncClient: SqsAsyncClient,
    private val objectMapper: ObjectMapper,
    @Value("\${spring.aws.sqs.queue-name.sheets-processing}") private val queueName: String,
) : GoltrixEventSenderPort {

    private val log = LoggerFactory.getLogger(GoltrixEventSender::class.java)

    private suspend fun getQueueUrl(): String {
        val response = sqsAsyncClient
            .getQueueUrl { it.queueName(queueName) }
            .await()

        return response.queueUrl()
    }

    override suspend fun send(goltrixDto: GoltrixDto) {

        val queueUrl = getQueueUrl()

        val json = objectMapper.writeValueAsString(goltrixDto)

        val request = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(json)
            .messageGroupId("${goltrixDto.betfairId}_${goltrixDto.alertName.replace("[^A-Za-z0-9]".toRegex(), "")}")
            .messageAttributes(
                mapOf(
                    "operation" to MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue("CREATE")
                        .build()
                )
            )
            .build()

        sqsAsyncClient.sendMessage(request).await()
        log.info("Message sent successfully → $json")
    }

    override suspend fun sendUpdate(goltrixUpdate: GoltrixUpdate) {
        val queueUrl = getQueueUrl()

        val json = objectMapper.writeValueAsString(goltrixUpdate)


        val request = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(json)
            .messageGroupId("${goltrixUpdate.betfairId}_${goltrixUpdate.alertName.replace("[^A-Za-z0-9]".toRegex(), "")}")
            .messageAttributes(
                mapOf(
                    "operation" to MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue("UPDATE")
                        .build()
                )
            )
            .build()

        sqsAsyncClient.sendMessage(request).await()
        log.info("Message Update sent successfully → $json")
    }

}
