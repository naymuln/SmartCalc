package com.bankasia.smartcalc.domain.validation

/**
 * Validation result for form inputs.
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errorMessage: String) : ValidationResult()
}