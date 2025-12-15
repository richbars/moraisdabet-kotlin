package com.richbars.moraisdabet.core.application.port

interface ChardrawServicePort {

    suspend fun saveGames()
    suspend fun update()

}