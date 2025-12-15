package com.richbars.moraisdabet.infrastructure.adapter.out

import com.fasterxml.jackson.databind.ObjectMapper
import com.richbars.moraisdabet.core.application.port.CachePort
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class CacheAdapter(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) : CachePort {

    override fun <T> get(key: String, clazz: Class<T>): T? {
        val value = redisTemplate.opsForValue().get(key)
            ?: return null

        return objectMapper.readValue(value, clazz)
    }

    override fun <T> put(key: String, value: T, ttl: Duration?) {
        val json = objectMapper.writeValueAsString(value)

        if (ttl != null) {
            redisTemplate.opsForValue().set(key, json, ttl)
        } else {
            redisTemplate.opsForValue().set(key, json)
        }
    }

    override fun delete(key: String) {
        redisTemplate.delete(key)
    }
}