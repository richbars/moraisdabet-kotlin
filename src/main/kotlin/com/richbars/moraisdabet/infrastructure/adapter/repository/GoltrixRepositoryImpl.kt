package com.richbars.moraisdabet.infrastructure.adapter.repository


import com.richbars.moraisdabet.core.application.dto.GoltrixDto
import com.richbars.moraisdabet.core.application.dto.GoltrixUpdate
import com.richbars.moraisdabet.core.application.port.GoltrixPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import toEntity
import toModel

@Repository
class GoltrixRepositoryImpl(
    private val jpaGoltrixRepository: JpaGoltrixRepository
) : GoltrixPort {

    private val log = LoggerFactory.getLogger(GoltrixRepositoryImpl::class.java)

    override suspend fun save(goltrix: GoltrixDto): Boolean {
        return try {
            val entity = goltrix.toEntity()
            jpaGoltrixRepository.save(entity)
            true
        } catch (e: Exception){
            log.warn("Conflito ao salvar jogo ${goltrix.eventName} - ${goltrix.alertName}: já existe (betfair_id + alert_name)", e)
            false
        }
    }


    override suspend fun findByBetfairIdAndAlertName(betfairId: Long, alertName: String): GoltrixDto? {
        return try {
            val entity = jpaGoltrixRepository.findByBetfairIdAndAlertName(betfairId, alertName)
            entity?.toModel()
        } catch (ex: Exception) {
            log.error("Erro ao buscar por betfairId: $betfairId e alertName: $alertName", ex)
            null
        }
    }

    override suspend fun getMatchsInProgress(): List<GoltrixDto> {
        return try {
            val entities = jpaGoltrixRepository.getMatchsInProgress()
            entities.map { it.toModel() }
        } catch (ex: Exception) {
            log.error("Erro ao buscar jogos em andamento", ex)
            emptyList()
        }
    }

    override suspend fun deleteByBetfairId(betfairId: Long, alertName: String) {
        return try {
            jpaGoltrixRepository.deleteByBetfairId(betfairId, alertName)
            log.info("Deletando jogo com betfairId: $betfairId da database!")
        } catch (ex: Exception) {
            log.error("Erro ao deletar jogo com betfairId: $betfairId", ex)
        }
    }

    override fun findAll(): List<GoltrixDto> {
        TODO()
    }

    override suspend fun updateGoltrix(goltrix: GoltrixUpdate): Boolean {
        return try {
            val rowsUpdated = jpaGoltrixRepository.updateGoltrix(
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


}
