package com.richbars.moraisdabet
import com.ninjasquad.springmockk.MockkBean
import com.richbars.moraisdabet.core.application.service.ChardrawService
import com.richbars.moraisdabet.infrastructure.adapter.out.PackballHttpAdapter
import com.richbars.moraisdabet.infrastructure.adapter.repository.GoltrixRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(locations = ["classpath:application.yml"])
class PackballHttpTest {


    @MockkBean
    lateinit var goltrixRepository: GoltrixRepository

    @Autowired
    lateinit var packballHttpAdapter: PackballHttpAdapter

    @Test
    fun `should login execute successfully`() = runTest {
        val token = packballHttpAdapter.login()
        assertTrue(token.isNotEmpty())
    }

}
