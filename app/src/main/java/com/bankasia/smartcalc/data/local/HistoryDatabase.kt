package com.bankasia.smartcalc.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for storing calculation history locally.
 * Data never leaves the device.
 */
@Database(
    entities = [CalculationHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HistoryDatabase : RoomDatabase() {
    abstract fun calculationHistoryDao(): CalculationHistoryDao
}