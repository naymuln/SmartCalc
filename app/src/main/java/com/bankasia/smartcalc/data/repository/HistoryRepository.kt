package com.bankasia.smartcalc.data.repository

import com.bankasia.smartcalc.data.local.CalculationHistoryEntity
import com.bankasia.smartcalc.data.local.CalculationHistoryDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing calculation history.
 * All data remains on-device; no server sync.
 */
@Singleton
class HistoryRepository @Inject constructor(
    private val calculationHistoryDao: CalculationHistoryDao
) {

    /** Get all calculation history, sorted by date descending */
    val allHistory: Flow<List<CalculationHistoryEntity>> = calculationHistoryDao.getAllHistory()

    /** Get history filtered by calculator type */
    fun getHistoryByType(type: String): Flow<List<CalculationHistoryEntity>> =
        calculationHistoryDao.getHistoryByType(type)

    /** Save a calculation to history */
    suspend fun saveCalculation(history: CalculationHistoryEntity) =
        calculationHistoryDao.insertHistory(history)

    /** Get a single calculation by ID */
    suspend fun getCalculationById(id: Long): CalculationHistoryEntity? =
        calculationHistoryDao.getHistoryById(id)

    /** Delete a calculation from history */
    suspend fun deleteCalculation(history: CalculationHistoryEntity) =
        calculationHistoryDao.deleteHistory(history)

    /** Clear all calculation history */
    suspend fun clearAllHistory() =
        calculationHistoryDao.clearAllHistory()

    /** Clear history for a specific calculator type */
    suspend fun clearHistoryByType(type: String) =
        calculationHistoryDao.clearHistoryByType(type)
}