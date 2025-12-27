package com.richbars.moraisdabet.core.application.service

import com.richbars.moraisdabet.core.application.port.CornerproServicePort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CornerproService : CornerproServicePort {
    override suspend fun execute() = withContext(Dispatchers.IO) {
        TODO("Not yet implemented")
    }

    override suspend fun update() {
        TODO("Not yet implemented")
    }

//    private suspend fun getToken() : String {
//
//
//
//    }

}