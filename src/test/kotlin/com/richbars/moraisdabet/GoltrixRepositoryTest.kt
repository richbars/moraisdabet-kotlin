//package com.richbars.moraisdabet
//
//import com.richbars.moraisdabet.core.application.dto.GoltrixDto
//import com.richbars.moraisdabet.core.application.port.GoltrixPort
//import com.richbars.moraisdabet.infrastructure.adapter.repository.GoltrixRepositoryImpl
//import com.richbars.moraisdabet.infrastructure.adapter.repository.JpaGoltrixRepository
//import com.richbars.moraisdabet.infrastructure.repository.entity.JpaGoltrixEntity
//import io.mockk.*
//import kotlinx.coroutines.runBlocking
//import org.junit.jupiter.api.Assertions.assertTrue
//import org.junit.jupiter.api.Test
//import java.time.LocalDate
//
//class GoltrixRepositoryTest {
//
//    private val jpaRepo = mockk<JpaGoltrixRepository>()
//    private val repository: GoltrixPort = GoltrixRepositoryImpl(jpaRepo)
//
//    @Test
//    fun `deve salvar GoltrixDto corretamente`() = runBlocking {
//
//        // Arrange: cria o DTO com os valores fornecidos
//        val dto = GoltrixDto(
//            betfairId = 34953901,
//            sofascoreId = 13233692,
//            eventName = "Spain v Türkiye",
//            leagueName = "FIFA World Cup Qualifiers - Europe",
//            homeName = "Spain",
//            awayName = "Türkiye",
//            date = LocalDate.of(2025, 11, 18),
//            alertMarketUnderName = null,
//            alertOddUnder = null,
//            alertMarketHtName = null,
//            alertOddHt = null,
//            marketUnderId = null,
//            marketHtId = null,
//            alertName = "Com Dados",
//            alertEntryMinute = 95,
//            alertEntryScore = "2-2",
//            alertExitMinute = null,
//            alertExitScore = null,
//            gameStatus = "inprogress",
//            goltrixStatus = null,
//            gameFinalScore = "2-2"
//        )
//
//        // Stub do repository JPA
//        coEvery { jpaRepo.insertWithConflict(any()) } returns Unit
//
//        // Act
//        val result = repository.save(dto)
//
//        // Assert
//        assertTrue(result)
//
//        coVerify(exactly = 1) {
//            jpaRepo.insertWithConflict(match { entity: JpaGoltrixEntity ->
//                entity.betfairId == dto.betfairId &&
//                        entity.sofascoreId == dto.sofascoreId &&
//                        entity.eventName == dto.eventName &&
//                        entity.alertName == dto.alertName &&
//                        entity.gameStatus == dto.gameStatus
//            })
//        }
//    }
//}
