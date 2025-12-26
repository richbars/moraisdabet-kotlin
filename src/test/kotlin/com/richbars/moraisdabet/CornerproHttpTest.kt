package com.richbars.moraisdabet
import com.ninjasquad.springmockk.MockkBean
import com.richbars.moraisdabet.core.application.service.ChardrawService
import com.richbars.moraisdabet.infrastructure.adapter.out.BetfairHttpAdapter
import com.richbars.moraisdabet.infrastructure.adapter.out.CornerproHttpAdapter
import com.richbars.moraisdabet.infrastructure.adapter.out.FulltraderHttpAdapter
import com.richbars.moraisdabet.infrastructure.adapter.repository.GoltrixRepository
import com.richbars.moraisdabet.infrastructure.http.HttpClientManager
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertTrue

@SpringBootTest(
    classes = [
        CornerproHttpAdapter::class
    ],
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration"
    ]
)
class CornerproHttpTest {

    @Autowired
    lateinit var cornerproHttpAdapter: CornerproHttpAdapter

    @Test
    fun `should get savedd filter cornerpro sucessfully`() = runTest {
        val filters = cornerproHttpAdapter.getGamesMatch()
        println(filters)
        assertNotNull(filters)
    }
}
