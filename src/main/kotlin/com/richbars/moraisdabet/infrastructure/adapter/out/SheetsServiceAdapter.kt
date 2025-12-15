package com.richbars.moraisdabet.infrastructure.adapter.out

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.richbars.moraisdabet.core.application.dto.ChardrawDto
import com.richbars.moraisdabet.core.application.dto.ChardrawUpdate
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

    @Value("\${spring.sheets.chardraw.sheet-name}")
    private lateinit var charcrawSheetName: String

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

                goltrixDto.marketName,         // 9 Mercado Lay
                goltrixDto.marketOdd,    // 10 Odd Lay
                goltrixDto.marketId,    // 11 Id Mercado Lay

                goltrixDto.alertEntryMinute?.toString() ?: "", // 12 Minuto Entrada Alerta
                goltrixDto.alertEntryScore,                    // 13 Placar Minuto Alerta

                goltrixDto.alertExitMinute ?: "",              // 14 Saída Minuto Alerta
                goltrixDto.alertExitScore ?: "",               // 15 Placar Saída

                goltrixDto.gameStatus,                         // 16 Status Partida
                goltrixDto.goltrixStatus ?: "",                // 17 Goltrix Status
                goltrixDto.gameFinalScore                      // 18 Placar Final
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

            val response = sheets.spreadsheets().values()
                .get(spreadsheetId, "$goltrixSheetName!A:R") // Existem 18 colunas (A..R)
                .execute()

            val rows = response.getValues() ?: emptyList()

            // Encontrar linha pelo betfairId + alertName
            val targetRowIndex = rows.indexOfFirst { row ->
                row.size >= 18 &&
                        row[0].toString() == goltrixUpdate.betfairId.toString() &&
                        row[7].toString().trim() == goltrixUpdate.alertName.trim()
            }

            if (targetRowIndex == -1) {
                logger.warn("Linha não encontrada no Sheets para betfairId=${goltrixUpdate.betfairId} e alertName=${goltrixUpdate.alertName}")
                return goltrixUpdate
            }

            val row = rows[targetRowIndex].toMutableList()

            // Atualizar colunas corretas
            row[13] = goltrixUpdate.alertExitMinute ?: ""   // Saída Minuto
            row[14] = goltrixUpdate.alertExitScore          // Placar Saída
            row[15] = goltrixUpdate.gameStatus              // Status Partida
            row[16] = goltrixUpdate.goltrixStatus           // Goltrix Status
            row[17] = goltrixUpdate.gameFinalScore          // Placar Final

            val body = ValueRange().setValues(listOf(row))

            val actualRowNumber = targetRowIndex + 1 // +1 por causa do cabeçalho

            sheets.spreadsheets().values()
                .update(
                    spreadsheetId,
                    "$goltrixSheetName!A$actualRowNumber:R$actualRowNumber",
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

    override suspend fun createChardrawRow(chardrawDto: ChardrawDto): ChardrawDto {
        return try {

            val sheets = getSheetsService()

            val row = listOf(
                chardrawDto.betfairId.toString(),
                chardrawDto.eventName,
                chardrawDto.leagueName,
                chardrawDto.homeName,
                chardrawDto.awayName,
                chardrawDto.date.toString(),
                chardrawDto.hour.toString(),
                chardrawDto.marketNameHT,
                chardrawDto.marketOddHT ,
                chardrawDto.marketNameFT ?: "",
                chardrawDto.marketOddFT ?: "",
                chardrawDto.statusHT ?: "",
                chardrawDto.statusFT ?: "",
                chardrawDto.gameStatus ?: ""
            )

            val body = ValueRange().setValues(listOf(row))

            sheets.spreadsheets().values()
                .append(spreadsheetId, "$charcrawSheetName!A:N", body)
                .setValueInputOption("RAW")
                .execute()

            logger.debug(
                "Chardraw record inserted successfully into Google Sheets. betfairId={}",
                chardrawDto.betfairId
            )

            chardrawDto

        } catch (e: Exception) {
            logger.error("Failed to insert Chardraw record into Google Sheets", e)
            throw RuntimeException("Failed to insert Chardraw data into Google Sheets", e)
        }
    }


    override suspend fun updateChardrawRow(chardrawUpdate: ChardrawUpdate) {
        return try {

            val sheets = getSheetsService()

            val response = sheets.spreadsheets().values()
                .get(spreadsheetId, "$charcrawSheetName!A:A")
                .execute()

            val rows = response.getValues()

            val rowIndex = rows.indexOfFirst { row ->
                row.isNotEmpty() && row[0].toString() == chardrawUpdate.betfairId.toString()
            }

            if (rowIndex == -1) {
                throw IllegalStateException(
                    "BetfairId ${chardrawUpdate.betfairId} não encontrado no Sheets"
                )
            }

            val sheetRow = rowIndex + 1

            val updatedValues = listOf(
                listOf(
                    chardrawUpdate.marketNameFT ?: "",
                    chardrawUpdate.marketOddFT ?: "",
                    chardrawUpdate.marketIdFT ?: "",
                    chardrawUpdate.statusFT ?: "",
                    chardrawUpdate.gameStatus ?: ""
                )
            )

            val body = ValueRange().setValues(updatedValues)

            sheets.spreadsheets().values()
                .update(
                    spreadsheetId,
                    "$charcrawSheetName!J$sheetRow:N$sheetRow",
                    body
                )
                .setValueInputOption("RAW")
                .execute()

            logger.debug(
                "Chardraw row updated successfully. betfairId={}, row={}",
                chardrawUpdate.betfairId,
                sheetRow
            )

        } catch (e: Exception) {
            logger.error("Failed to update Chardraw record in Google Sheets")
            throw RuntimeException("Failed to update Chardraw data in Google Sheets", e)
        }
    }


}
