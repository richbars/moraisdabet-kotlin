package com.richbars.moraisdabet.core.application.port

interface SofascoreHttptPort {
    suspend fun getEventNameById(query: String): Long
    suspend fun getCurrentGameMinuteById(sofascoreId: Long): Int?
    suspend fun getStatusGameById(sofascoreId: Long): String
    suspend fun getScoreGameById(sofascoreId: Long): String
}