package com.richbars.moraisdabet
import com.ninjasquad.springmockk.MockkBean
import com.richbars.moraisdabet.core.application.service.ChardrawService
import com.richbars.moraisdabet.infrastructure.adapter.repository.GoltrixRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration"
    ]
)
class ChardrawTest {

    @MockkBean
    lateinit var goltrixRepository: GoltrixRepository

    @Autowired
    lateinit var chardrawService: ChardrawService

    @Test
    fun `should saveGamesChardraw execute successfully`() = runTest {
        chardrawService.saveGames()
    }
}
