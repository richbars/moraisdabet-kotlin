package com.richbars.moraisdabet.infrastructure.adapter.repository

import com.richbars.moraisdabet.infrastructure.repository.entity.ChardrawEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ChardrawRepository : CoroutineCrudRepository<ChardrawEntity, Long> {
}

