package com.bankasia.smartcalc.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing calculation history.
 * Stores calculator type, inputs, result, and metadata locally on device.
 */
@Entity(tableName = "calculation_history")
data class CalculationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val calculatorType: String, // "emi", "personal_loan_dvr", "credit_card_dvr"
    val dateTime: String, // ISO 8601 timestamp
    val mainInputs: String, // JSON string of main inputs
    val mainResult: String, // Main result value
    val resultCategory: String?, // "eligible", "not_eligible", "emi_amount", etc.
    val detailsJson: String? // Full calculation breakdown as JSON
)