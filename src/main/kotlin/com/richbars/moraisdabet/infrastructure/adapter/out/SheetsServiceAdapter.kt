package com.richbars.moraisdabet.infrastructure.adapter.out

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.richbars.moraisdabet.core.application.dto.GoltrixDto
import com.richbars.moraisdabet.core.application.dto.GoltrixUpdate
import com.richbars.moraisdabet.core.application.port.SheetsServicePort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class SheetsServiceAdapter : SheetsServicePort {

    private val logger = LoggerFactory.getLogger(SheetsServiceAdapter::class.java)

    @Value("\${spring.sheets.credentials.path}")
    private lateinit var credentialsPath: String

    @Value("\${spring.sheets.id}")
    private lateinit var spreadsheetId: String

    @Value("\${spring.sheets.goltrix.sheet-name}")
    private lateinit var goltrixSheetName: String

    companion object {
        private const val APPLICATION_NAME = "MoraisdaBet Sheets Service"
        private val SCOPES = listOf(SheetsScopes.SPREADSHEETS)
        private val JSON_FACTORY = GsonFactory.getDefaultInstance();
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }

    private fun getSheetsService(): Sheets {
        return try {
            val httpTransport = GoogleNetHttpTransport.newTrustedTransport()

            val stream = this::class.java.classLoader.getResourceAsStream(credentialsPath)
                ?: throw RuntimeException("Arquivo de credenciais não encontrado no classpath: $credentialsPath")

            val credentials = GoogleCredentials.fromStream(stream).createScoped(SCOPES)

            Sheets.Builder(httpTransport, JSON_FACTORY, HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build()

        } catch (e: Exception) {
            logger.error("Erro ao inicializar Google Sheets Service", e)
            throw RuntimeException("Falha ao inicializar serviço do Google Sheets", e)
        }
    }


    override suspend fun createGoltrixRow(goltrixDto: GoltrixDto): GoltrixDto {
        return try {
            val sheets = getSheetsService()

            val row = listOf(
                goltrixDto.betfairId.toString(),               // 1 ID
                goltrixDto.sofascoreId.toString(),             // 2 SOFASCORE ID
                goltrixDto.eventName,                          // 3 Evento
                goltrixDto.leagueName,                         // 4 Liga
                goltrixDto.homeName,                           // 5 Casa
                goltrixDto.awayName,                           // 6 Visitante
                goltrixDto.date.format(DATE_FORMATTER),        // 7 Data

                goltrixDto.alertName,                          // 8 Nome Alerta

                goltrixDto.alertMarketUnderName ?: "",         // 9 Mercado Lay
                goltrixDto.alertOddUnder?.toString() ?: "",    // 10 Odd Lay
                goltrixDto.marketUnderId?.toString() ?: "",    // 11 Id Mercado Lay

                goltrixDto.alertMarketHtName ?: "",            // 12 Mercado HT
                goltrixDto.alertOddHt?.toString() ?: "",       // 13 Odd HT
                goltrixDto.marketHtId?.toString() ?: "",       // 14 Id Mercado HT

                goltrixDto.alertEntryMinute?.toString() ?: "", // 15 Minuto Entrada Alerta
                goltrixDto.alertEntryScore,                    // 16 Placar Minuto Alerta

                goltrixDto.alertExitMinute ?: "",              // 17 Saída Minuto Alerta
                goltrixDto.alertExitScore ?: "",               // 18 Placar Saída

                goltrixDto.gameStatus,                         // 19 Status Partida
                goltrixDto.goltrixStatus ?: "",                // 20 Goltrix Status
                goltrixDto.gameFinalScore                      // 21 Placar Final
            )


            val body = ValueRange().setValues(listOf(row))

            // Adiciona no final da aba sem sobrescrever
            sheets.spreadsheets().values()
                .append(spreadsheetId, "$goltrixSheetName!A:Z", body)
                .setValueInputOption("RAW")
                .execute()

            logger.info("Linha Goltrix adicionada com sucesso")
            goltrixDto

        } catch (e: Exception) {
            logger.error("Erro ao inserir linha Goltrix no Google Sheets", e)
            throw RuntimeException("Falha ao inserir no Google Sheets", e)
        }
    }

    override suspend fun updateGoltrixRow(goltrixUpdate: GoltrixUpdate): GoltrixUpdate {
        return try {
            val sheets = getSheetsService()

            // 1. Buscar conteúdo da planilha
            val response = sheets.spreadsheets().values()
                .get(spreadsheetId, "$goltrixSheetName!A:Z")
                .execute()

            val rows = response.getValues() ?: emptyList()

            // 2. Identificar a linha correta (match por betfairId + alertName)
            val targetRowIndex = rows.indexOfFirst { row ->
                row.size >= 21 &&
                        row[0].toString() == goltrixUpdate.betfairId.toString() &&   // CORRIGIDO
                        row[7].toString().trim() == goltrixUpdate.alertName.trim()  // CORRIGIDO
            }

            if (targetRowIndex == -1) {
                logger.warn("Linha não encontrada no Sheets para betfairId=${goltrixUpdate.betfairId} e alertName=${goltrixUpdate.alertName}")
                return goltrixUpdate
            }

            // 3. Clonar a linha original
            val row = rows[targetRowIndex].toMutableList()

            // 4. Atualizar colunas corretas
            row[16] = goltrixUpdate.alertExitMinute ?: ""   // Saída Minuto
            row[17] = goltrixUpdate.alertExitScore          // Placar Saída
            row[18] = goltrixUpdate.gameStatus              // Status Partida
            row[19] = goltrixUpdate.goltrixStatus           // Goltrix Status
            row[20] = goltrixUpdate.gameFinalScore          // Placar Final

            // 5. Criar ValueRange com a linha atualizada
            val body = ValueRange().setValues(listOf(row))

            // 6. Calcular o número real da linha (+1 pelo cabeçalho)
            val actualRowNumber = targetRowIndex + 1

            // 7. Atualizar no Google Sheets
            sheets.spreadsheets().values()
                .update(
                    spreadsheetId,
                    "$goltrixSheetName!A$actualRowNumber:Z$actualRowNumber",
                    body
                )
                .setValueInputOption("RAW")
                .execute()

            logger.info("Linha atualizada com sucesso na planilha (row=$actualRowNumber)")

            goltrixUpdate

        } catch (e: Exception) {
            logger.error("Erro ao atualizar linha Goltrix no Google Sheets", e)
            throw RuntimeException("Falha ao atualizar no Google Sheets", e)
        }
    }

}
