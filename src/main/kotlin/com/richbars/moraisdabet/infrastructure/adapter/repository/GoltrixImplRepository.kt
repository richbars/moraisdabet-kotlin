package com.richbars.moraisdabet.infrastructure.adapter.repository


import com.richbars.moraisdabet.core.application.dto.GoltrixDto
import com.richbars.moraisdabet.core.application.dto.GoltrixUpdate
import com.richbars.moraisdabet.core.application.port.GoltrixPort
import com.richbars.moraisdabet.infrastructure.adapter.mapper.toEntity
import com.richbars.moraisdabet.infrastructure.adapter.mapper.toModel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class GoltrixImplRepository(
    private val goltrixRepository: GoltrixRepository
) : GoltrixPort {

    private val log = LoggerFactory.getLogger(GoltrixImplRepository::class.java)

    override suspend fun save(goltrix: GoltrixDto): Boolean {
        return try {
            val entity = goltrix.toEntity()
            goltrixRepository.save(entity)
            true
        } catch (e: Exception){
            log.warn("Conflito ao salvar jogo ${goltrix.eventName} - ${goltrix.alertName}: já existe (betfair_id + alert_name)", e)
            false
        }
    }


    override suspend fun findByBetfairIdAndAlertName(betfairId: Long, alertName: String): GoltrixDto? {
        return try {
            val entity = goltrixRepository.findByBetfairIdAndAlertName(betfairId, alertName)
            entity?.toModel()
        } catch (ex: Exception) {
            log.error("Erro ao buscar por betfairId: $betfairId e alertName: $alertName", ex)
            null
        }
    }

    override suspend fun getMatchsInProgress(): List<GoltrixDto> {
        return try {
            val entities = goltrixRepository.getMatchsInProgress()
            entities.map { it.toModel() }
        } catch (ex: Exception) {
            log.error("Erro ao buscar jogos em andamento", ex)
            emptyList()
        }
    }

    override suspend fun deleteByBetfairId(betfairId: Long, alertName: String) {
        return try {
            goltrixRepository.deleteByBetfairId(betfairId, alertName)
            log.info("Deletando jogo com betfairId: $betfairId da database!")
        } catch (ex: Exception) {
            log.error("Erro ao deletar jogo com betfairId: $betfairId", ex)
        }
    }

    override suspend fun findAll(): List<GoltrixDto> {
        return try {
            goltrixRepository.getAll().map { it.toModel() }
        } catch (ex: Exception){
            log.error("Erro ao buscar todos os jogos", ex)
            emptyList()
        }
    }

    override suspend fun updateGoltrix(goltrix: GoltrixUpdate): Boolean {
        return try {
            val rowsUpdated = goltrixRepository.updateGoltrix(
                betfairId = goltrix.betfairId,
                alertName = goltrix.alertName,
                alertExitMinute = goltrix.alertExitMinute,
                alertExitScore = goltrix.alertExitScore,
                gameStatus = goltrix.gameStatus,
                goltrixStatus = goltrix.goltrixStatus,
                gameFinalScore = goltrix.gameFinalScore
            )

            if (rowsUpdated > 0) {
                log.info(
                    "Goltrix atualizado com sucesso | betfairId=${goltrix.betfairId} | alertName=${goltrix.alertName}"
                )
                true
            } else {
                log.info(
                    "Nenhuma linha atualizada para betfairId=${goltrix.betfairId} e alertName=${goltrix.alertName}"
                )
                false
            }

        } catch (ex: Exception) {
            log.error(
                "Erro ao atualizar jogo com betfairId=${goltrix.betfairId} e alertName=${goltrix.alertName}: ${ex.message}"
            )
            false
        }
    }

    override suspend fun verifyExit(): List<GoltrixDto> {
        return try {
            goltrixRepository.verifyExit()
                .map { it.toModel() }
        } catch (ex: Exception) {
            log.error("Erro ao verificar saída do jogo", ex)
            throw ex
        }
    }


}
