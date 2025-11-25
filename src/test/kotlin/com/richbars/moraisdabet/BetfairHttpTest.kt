package com.richbars.moraisdabet

import com.ninjasquad.springmockk.MockkBean
import com.richbars.moraisdabet.infrastructure.adapter.out.BetfairHttpAdapter
import com.richbars.moraisdabet.infrastructure.http.HttpClientManager
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    classes = [BetfairHttpAdapter::class], // apenas o componente que contém getMarketById
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration"
    ]
)
@ActiveProfiles("test")
class BetfairHttpTest {
    @MockkBean
    lateinit var httpClientManager: HttpClientManager

    @Test
    fun `should load spring and call getMarketById successfully`() = runTest {
        val betfairHttpAdapter = BetfairHttpAdapter()

        val t = betfairHttpAdapter.getMarketById(34993626, "First Half")
        println(t)
    }
}