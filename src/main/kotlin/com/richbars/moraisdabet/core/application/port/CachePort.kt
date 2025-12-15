package com.richbars.moraisdabet.core.application.port

import java.time.Duration

interface CachePort {

    fun <T> get(key: String, clazz: Class<T>): T?

    fun <T> put(
        key: String,
        value: T,
        ttl: Duration? = null
    )

    fun delete(key: String)
}