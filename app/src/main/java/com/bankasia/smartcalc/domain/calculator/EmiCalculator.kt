package com.bankasia.smartcalc.domain.calculator

import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.math.MathContext
import java.math.RoundingMode

/**
 * Standard reducing-balance EMI Calculator.
 *
 * Formula:
 *   EMI = P × r × (1+r)^n / ((1+r)^n - 1)
 *
 * where:
 *   P = principal
 *   r = monthly interest rate (annual rate / 1200)
 *   n = number of installments
 *
 * Handles 0% interest correctly (EMI = P / n).
 */
object EmiCalculator {

    private val MC = MathContext(16, RoundingMode.HALF_UP)
    private val DISPLAY_SCALE = 2

    data class Input(
        val principal: BigDecimal,
        val annualInterestRate: BigDecimal,
        val tenure: Int,
        val tenureUnit: TenureUnit
    )

    data class Result(
        val principal: BigDecimal,
        val totalInterest: BigDecimal,
        val totalRepayment: BigDecimal,
        val monthlyEmi: BigDecimal,
        val monthlyEmiDisplay: String
    )

    enum class TenureUnit { MONTHS, YEARS }

    fun calculate(input: Input): Result {
        require(input.principal > ZERO) { "Principal must be greater than zero" }
        require(input.tenure > 0) { "Tenure must be greater than zero" }
        require(input.annualInterestRate >= ZERO) { "Interest rate cannot be negative" }

        val n = if (input.tenureUnit == TenureUnit.YEARS) {
            input.tenure * 12
        } else {
            input.tenure
        }

        val r = input.annualInterestRate.divide(BigDecimal.valueOf(1200), MC)

        val monthlyEmi = if (r == ZERO) {
            input.principal.divide(BigDecimal.valueOf(n.toLong()), DISPLAY_SCALE, RoundingMode.HALF_UP)
        } else {
            val onePlusR = BigDecimal.ONE.add(r)
            val power = onePlusR.pow(n, MC)
            val numerator = input.principal.multiply(r).multiply(power)
            val denominator = power.subtract(BigDecimal.ONE)
            numerator.divide(denominator, DISPLAY_SCALE, RoundingMode.HALF_UP)
        }

        val totalPayment = if (r == ZERO) input.principal else monthlyEmi.multiply(BigDecimal.valueOf(n.toLong()))
        val totalInterest = if (r == ZERO) ZERO else totalPayment.subtract(input.principal).max(ZERO)

        return Result(
            principal = input.principal,
            totalInterest = totalInterest,
            totalRepayment = totalPayment,
            monthlyEmi = monthlyEmi,
            monthlyEmiDisplay = formatCurrency(monthlyEmi)
        )
    }

    private fun formatCurrency(value: BigDecimal): String {
        return com.bankasia.smartcalc.domain.util.IndianNumberFormatter.formatIndianCurrency(value)
    }

    private fun formatNumber(value: BigDecimal): String {
        return com.bankasia.smartcalc.domain.util.IndianNumberFormatter.formatIndianNumber(value)
    }
}