package com.bankasia.smartcalc.domain.calculator

import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.math.MathContext
import java.math.RoundingMode

/**
 * Personal Loan DVR Calculator implementing Bank Asia Personal Loan PPD-2026 methodology.
 *
 * The PPD gives the capacity-to-pay formula:
 *
 * Personal Loan DVR numerator:
 *   1. EMIs of all existing installment-based loans of Bank Asia and other banks/FIs
 *   2. Average monthly interest of CC/Overdraft loans
 *   3. 3% of credit-card limit
 *   4. Proposed Personal Loan installment
 *
 * Then:
 *   DVR = Total considered monthly obligations / Sum of all considered income × 100
 *
 * DO NOT substitute another formula. The PPD explicitly states this methodology.
 */
object PersonalLoanDvrCalculator {

    private val MC = MathContext(16, RoundingMode.HALF_UP)
    private val DISPLAY_SCALE = 2

    /** +10% DVR relaxation for own house/father's house */
    private const val OWN_HOUSE_RELAXATION = 10.0

    /** PPD-defined DVR slabs for Category A & B customers */
    private val dvrSlabsCategoryAB = listOf(
        DVR_SLAB(20000, 34999, 35.0),
        DVR_SLAB(35000, 54999, 40.0),
        DVR_SLAB(55000, 69999, 50.0),
        DVR_SLAB(70000, 99999, 55.0),
        DVR_SLAB(100000, 149999, 60.0),
        DVR_SLAB(150000, Int.MAX_VALUE, 65.0)
    )

    /** PPD-defined DVR slabs for Bank Asia Employee customers */
    private val dvrSlabsEmployee = listOf(
        DVR_SLAB(20000, 29999, 35.0),
        DVR_SLAB(30000, 49999, 40.0),
        DVR_SLAB(50000, 69999, 50.0),
        DVR_SLAB(70000, 99999, 55.0),
        DVR_SLAB(100000, 149999, 60.0),
        DVR_SLAB(150000, Int.MAX_VALUE, 65.0)
    )

    data class DVR_SLAB(
        val minIncome: Int,
        val maxIncome: Int,
        val maxDvrPercent: Double
    )

    /** Customer category: Category A/B or Employee */
    enum class CustomerCategory { CategoryAB, Employee }

    /** Calculate EMI for proposed personal loan */
    fun calculateProposedLoanEmi(
        principal: BigDecimal,
        annualInterestRate: BigDecimal,
        tenureMonths: Int
    ): BigDecimal {
        require(principal > ZERO) { "Principal must be greater than zero" }
        require(tenureMonths > 0) { "Tenure must be greater than zero" }
        require(annualInterestRate >= ZERO) { "Interest rate cannot be negative" }

        val r = annualInterestRate.divide(BigDecimal.valueOf(1200), MC)

        return if (r == ZERO) {
            principal.divide(BigDecimal.valueOf(tenureMonths.toLong()), DISPLAY_SCALE, RoundingMode.HALF_UP)
        } else {
            val onePlusR = BigDecimal.ONE.add(r)
            val power = onePlusR.pow(tenureMonths, MC)
            val numerator = principal.multiply(r).multiply(power)
            val denominator = power.subtract(BigDecimal.ONE)
            numerator.divide(denominator, DISPLAY_SCALE, RoundingMode.HALF_UP)
        }
    }

    /** Calculate 3% of credit card limit */
    fun calculateCardLiability(totalLimit: BigDecimal): BigDecimal {
        return totalLimit.multiply(BigDecimal.valueOf(0.03), MC)
            .setScale(DISPLAY_SCALE, RoundingMode.HALF_UP)
    }

    /** Get permitted DVR from grid based on category and income */
    fun getPermittedDvr(
        category: CustomerCategory,
        monthlyIncome: BigDecimal
    ): Double {
        val slabs = when (category) {
            CustomerCategory.CategoryAB -> dvrSlabsCategoryAB
            CustomerCategory.Employee -> dvrSlabsEmployee
        }

        val incomeValue = monthlyIncome.toInt()

        return slabs.find { slab ->
            incomeValue >= slab.minIncome && incomeValue <= slab.maxIncome
        }?.maxDvrPercent
            ?: slabs.last().maxDvrPercent
    }

    /** Apply +10% own house relaxation if eligible */
    fun applyOwnHouseRelaxation(
        baseDvr: Double,
        ownsOwnHouse: Boolean,
        hasHomeLoan: Boolean
    ): Double {
        return if (ownsOwnHouse && !hasHomeLoan) {
            (baseDvr + OWN_HOUSE_RELAXATION).coerceAtMost(100.0)
        } else {
            baseDvr
        }
    }

    /** Main DVR calculation */
    fun calculateDvr(
        monthlyIncome: BigDecimal,
        existingLoanEmis: List<BigDecimal>,
        ccAverageMonthlyInterest: BigDecimal,
        totalCreditCardLimit: BigDecimal,
        proposedLoanEmi: BigDecimal,
        ownsOwnHouse: Boolean,
        hasHomeLoan: Boolean,
        customerCategory: CustomerCategory
    ): DvrResult {
        // 1. Calculate total considered monthly obligations (numerator)
        val existingEmisSum = existingLoanEmis.fold(ZERO) { a, b -> a.add(b, MC) }
        val ccThreePercent = calculateCardLiability(totalCreditCardLimit)
        val averageMonthlyInterest = ccAverageMonthlyInterest.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP)

        val totalObligations = existingEmisSum
            .add(averageMonthlyInterest)
            .add(ccThreePercent)
            .add(proposedLoanEmi)

        // 2. Sum of all considered income (denominator)
        val totalIncome = monthlyIncome

        // 3. Calculate DVR percentage
        val dvrValue = if (totalIncome > ZERO) {
            totalObligations
                .divide(totalIncome, MC)
                .multiply(BigDecimal.valueOf(100))
                .setScale(DISPLAY_SCALE, RoundingMode.HALF_UP)
        } else {
            ZERO
        }

        // 4. Get permitted DVR from grid
        val permittedDvr = getPermittedDvr(customerCategory, totalIncome)

        // 5. Apply +10% own house relaxation if eligible
        val finalPermittedDvr = applyOwnHouseRelaxation(permittedDvr, ownsOwnHouse, hasHomeLoan)

        return DvrResult(
            dvrValue = dvrValue,
            permittedDvr = permittedDvr,
            finalPermittedDvr = finalPermittedDvr,
            totalMonthlyObligations = totalObligations,
            totalConsideredIncome = totalIncome,
            existingLoanEmis = existingLoanEmis,
            creditCardThreePercentLiability = ccThreePercent,
            averageMonthlyInterest = averageMonthlyInterest,
            proposedLoanEmi = proposedLoanEmi
        )
    }

    /** DVR calculation result with all breakdown components */
    data class DvrResult(
        val dvrValue: BigDecimal,
        val permittedDvr: Double,
        val finalPermittedDvr: Double,
        val totalMonthlyObligations: BigDecimal,
        val totalConsideredIncome: BigDecimal,
        val existingLoanEmis: List<BigDecimal>,
        val creditCardThreePercentLiability: BigDecimal,
        val averageMonthlyInterest: BigDecimal,
        val proposedLoanEmi: BigDecimal
    )
}