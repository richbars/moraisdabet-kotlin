package com.richbars.moraisdabet.core.application.port

interface SheetsEventListenerPort {
    fun onMessage(message: String)
}