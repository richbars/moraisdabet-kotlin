package com.richbars.moraisdabet.infrastructure.adapter.messaging.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import java.net.URI

@Configuration
class SqsClientConfig(

    @Value("\${spring.aws.sqs.endpoint}") private val endpoint: String,
    @Value("\${spring.aws.credentials.access-key}") private val accessKey: String,
    @Value("\${spring.aws.credentials.secret-key}") private val secretKey: String,
    @Value("\${spring.aws.region}") private val region: String

) {

    companion object {
        private val log = LoggerFactory.getLogger(SqsClientConfig::class.java)
    }

    @Bean
    fun sqsAsyncClient(): SqsAsyncClient {
        log.info("Initializing SqsAsyncClient for region $region")

        val creds = AwsBasicCredentials.create(accessKey, secretKey)

        val client = SqsAsyncClient.builder()
            .region(Region.of(region))
            .endpointOverride(URI.create(endpoint)) // <- ESSENCIAL PARA LOCALSTACK
            .credentialsProvider(StaticCredentialsProvider.create(creds))
            .build()

        log.info("SqsAsyncClient successfully initialized")
        return client
    }
}