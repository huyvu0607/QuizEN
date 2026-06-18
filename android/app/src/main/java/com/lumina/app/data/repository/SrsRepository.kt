// SrsRepository.kt
package com.lumina.app.data.repository

import com.lumina.app.data.model.*
import com.lumina.app.data.source.local.dao.SrsDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SrsRepository(private val srsDao: SrsDao) {

    fun getDueCards(userId: Long): Flow<List<SrsCard>> =
        srsDao.getDueCards(userId, System.currentTimeMillis()).map { list ->
            list.map { it.toModel() }
        }

    fun countDueCards(userId: Long): Flow<Int> =
        srsDao.countDueCards(userId, System.currentTimeMillis())

    suspend fun getCard(userId: Long, vocabId: Long): SrsCard? =
        srsDao.getCard(userId, vocabId)?.toModel()

    suspend fun saveCard(card: SrsCard) =
        srsDao.insertCard(card.toEntity())

    suspend fun updateCard(card: SrsCard) =
        srsDao.updateCard(card.toEntity())
}