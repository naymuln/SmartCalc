package com.bankasia.smartcalc.di

import android.content.Context
import androidx.room.Room
import com.bankasia.smartcalc.data.local.CalculationHistoryDao
import com.bankasia.smartcalc.data.local.HistoryDatabase
import com.bankasia.smartcalc.data.repository.HistoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HiltModules {

    @Provides
    @Singleton
    fun provideHistoryDatabase(
        @ApplicationContext context: Context
    ): HistoryDatabase {
        return Room.databaseBuilder(
            context,
            HistoryDatabase::class.java,
            "smartcalc_history.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideCalculationHistoryDao(
        database: HistoryDatabase
    ): CalculationHistoryDao {
        return database.calculationHistoryDao()
    }

    @Provides
    @Singleton
    fun provideHistoryRepository(
        calculationHistoryDao: CalculationHistoryDao
    ): HistoryRepository {
        return HistoryRepository(calculationHistoryDao)
    }
}