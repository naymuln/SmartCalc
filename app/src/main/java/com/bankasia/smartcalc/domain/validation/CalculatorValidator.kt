package com.bankasia.smartcalc.domain.validation

import java.math.BigDecimal
import java.math.BigDecimal.ZERO

/**
 * Validation rules for calculator inputs.
 * Prevents invalid financial calculations and provides user-friendly error messages.
 */
object CalculatorValidator {

    /** Validate loan amount / principal */
    fun validatePrincipal(value: BigDecimal?): ValidationResult {
        if (value == null) return ValidationResult.Invalid("Loan amount is required")
        if (value < ZERO) return ValidationResult.Invalid("Loan amount cannot be negative")
        if (value == ZERO) return ValidationResult.Invalid("Loan amount must be greater than zero")
        return ValidationResult.Valid
    }

    /** Validate annual interest rate */
    fun validateInterestRate(value: BigDecimal?): ValidationResult {
        if (value == null) return ValidationResult.Invalid("Interest rate is required")
        if (value < ZERO) return ValidationResult.Invalid("Interest rate cannot be negative")
        return ValidationResult.Valid
    }

    /** Validate tenure in months */
    fun validateTenure(months: Int?): ValidationResult {
        if (months == null) return ValidationResult.Invalid("Tenure is required")
        if (months <= 0) return ValidationResult.Invalid("Tenure must be greater than zero")
        if (months > 600) return ValidationResult.Invalid("Tenure cannot exceed 50 years (600 months)")
        return ValidationResult.Valid
    }

    /** Validate monthly income */
    fun validateMonthlyIncome(value: BigDecimal?): ValidationResult {
        if (value == null) return ValidationResult.Invalid("Monthly income is required")
        if (value < ZERO) return ValidationResult.Invalid("Monthly income cannot be negative")
        if (value == ZERO) return ValidationResult.Invalid("Monthly income must be greater than zero")
        return ValidationResult.Valid
    }

    /** Validate credit card limit */
    fun validateCreditCardLimit(value: BigDecimal?): ValidationResult {
        if (value == null) return ValidationResult.Invalid("Credit card limit is required")
        if (value < ZERO) return ValidationResult.Invalid("Credit card limit cannot be negative")
        return ValidationResult.Valid
    }

    /** Validate monthly installment */
    fun validateMonthlyInstallment(value: BigDecimal?): ValidationResult {
        if (value == null) return ValidationResult.Invalid("Monthly installment is required")
        if (value < ZERO) return ValidationResult.Invalid("Monthly installment cannot be negative")
        return ValidationResult.Valid
    }

    /** Validate proposed credit card limit */
    fun validateProposedCardLimit(value: BigDecimal?): ValidationResult {
        if (value == null) return ValidationResult.Invalid("Proposed credit card limit is required")
        if (value < ZERO) return ValidationResult.Invalid("Proposed credit card limit cannot be negative")
        return ValidationResult.Valid
    }

    /** Validate average outstanding */
    fun validateAverageOutstanding(value: BigDecimal?): ValidationResult {
        if (value == null) return ValidationResult.Invalid("Average outstanding is required")
        if (value < ZERO) return ValidationResult.Invalid("Average outstanding cannot be negative")
        return ValidationResult.Valid
    }

    /** Validate net monthly salary */
    fun validateNetSalary(value: BigDecimal?): ValidationResult {
        if (value == null) return ValidationResult.Invalid("Monthly net salary is required")
        if (value < ZERO) return ValidationResult.Invalid("Net salary cannot be negative")
        if (value == ZERO) return ValidationResult.Invalid("Net salary must be greater than zero")
        return ValidationResult.Valid
    }

    /** Validate email format for contact info */
    fun validateEmail(email: String?): ValidationResult {
        if (email.isNullOrBlank()) return ValidationResult.Valid // optional
        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9._-]+\\.[a-zA-Z]+"
        return if (email.matches(Regex(emailPattern))) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("Invalid email format")
        }
    }

    /** Validate phone number (Bangladesh format) */
    fun validatePhoneNumber(phone: String?): ValidationResult {
        if (phone.isNullOrBlank()) return ValidationResult.Valid // optional
        // Bangladesh mobile numbers: 01XXXXXXXXX
        val phonePattern = "01[3-9]\\d{8}"
        return if (phone.matches(Regex(phonePattern))) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("Invalid Bangladesh phone number (e.g., 01XXXXXXXXX)")
        }
    }
}