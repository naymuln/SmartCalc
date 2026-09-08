package com.bankasia.smartcalc.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for calculation history database.
 */
@Dao
interface CalculationHistoryDao {

    @Query("SELECT * FROM calculation_history ORDER BY dateTime DESC")
    fun getAllHistory(): Flow<List<CalculationHistoryEntity>>

    @Query("SELECT * FROM calculation_history WHERE calculatorType = :type ORDER BY dateTime DESC")
    fun getHistoryByType(type: String): Flow<List<CalculationHistoryEntity>>

    @Query("SELECT * FROM calculation_history WHERE id = :id")
    suspend fun getHistoryById(id: Long): CalculationHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: CalculationHistoryEntity): Long

    @Delete
    suspend fun deleteHistory(history: CalculationHistoryEntity)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAllHistory()

    @Query("DELETE FROM calculation_history WHERE calculatorType = :type")
    suspend fun clearHistoryByType(type: String)
}