package com.richbars.moraisdabet

import com.ninjasquad.springmockk.MockkBean
import com.richbars.moraisdabet.infrastructure.adapter.out.BetfairHttpAdapter
import com.richbars.moraisdabet.infrastructure.adapter.out.FulltraderHttpAdapter
import com.richbars.moraisdabet.infrastructure.http.HttpClientManager
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@SpringBootTest(
    classes = [
        BetfairHttpAdapter::class,
        FulltraderHttpAdapter::class,
        HttpClientManager::class
    ],
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration"
    ]
)
@ActiveProfiles("test")
class BetfairHttpTest {

    @Autowired
    lateinit var fulltraderHttpAdapter: FulltraderHttpAdapter

    @Autowired
    lateinit var httpClientManager: HttpClientManager

    @Test
    fun `should load spring and call getMarketById successfully`() = runTest {
        val adapter = BetfairHttpAdapter()
        val t = adapter.getMarketByIdGoltrix(34949352, "Over HT Rodrigo")
        println(t)
    }

    @Test
    fun `shxxssxy`() = runTest {
        val t = fulltraderHttpAdapter.getGamesChardraw()
        println(t)
    }
}

