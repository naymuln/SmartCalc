package com.bankasia.smartcalc.domain.calculator

import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.math.MathContext
import java.math.RoundingMode

/**
 * Credit Card DVR Calculator implementing Bank Asia Credit Card PPG methodology.
 *
 * The PPG defines:
 *
 * DBR = (
 *   Existing Loan EMIs
 *   + 5% of Proposed Credit Card Limits
 *   + the higher of:
 *       3% of Existing Credit Card Limit
 *       OR
 *       5% of Last One-Year Average Outstanding
 * )
 * /
 * Monthly Net Salary
 * × 100
 *
 * This formula MUST be implemented exactly.
 * Do not confuse this with the Personal Loan formula.
 */
object CreditCardDvrCalculator {

    private val MC = MathContext(16, RoundingMode.HALF_UP)
    private val DISPLAY_SCALE = 2

    data class Input(
        val monthlyNetSalary: BigDecimal,
        val existingLoanEmis: List<ExistingLoanInput>,
        val proposedCardLimit: BigDecimal,
        val existingCards: List<ExistingCardInput>
    )

    data class ExistingLoanInput(
        val institution: String,
        val monthlyEmi: BigDecimal
    )

    data class ExistingCardInput(
        val creditLimit: BigDecimal,
        val lastOneYearAverageOutstanding: BigDecimal
    )

    data class Result(
        val dbrValue: BigDecimal,
        val totalMonthlyObligations: BigDecimal,
        val totalNetSalary: BigDecimal,
        val existingLoanEmis: BigDecimal,
        val fivePercentProposedCardLimit: BigDecimal,
        val threePercentExistingCardLimits: BigDecimal,
        val fivePercentAverageOutstanding: BigDecimal,
        val higherExistingCardLiability: BigDecimal,
        val maximumAllowedDvr: Double,
        val isEligible: Boolean
    )

    /** DBR calculation as per Bank Asia Credit Card PPG */
    fun calculateDbr(input: Input): Result {
        require(input.monthlyNetSalary > ZERO) { "Monthly net salary must be greater than zero" }

        // A. Existing Loan EMIs
        val existingLoanEmisSum = input.existingLoanEmis.fold(ZERO) { a, b -> a.add(b.monthlyEmi, MC) }

        // B. 5% of Proposed Credit Card Limit
        val fivePercentProposedCardLimit = input.proposedCardLimit
            .multiply(BigDecimal.valueOf(0.05), MC)
            .setScale(DISPLAY_SCALE, RoundingMode.HALF_UP)

        // C. Existing Credit Cards - calculate both options
        // Option 1: 3% of total existing credit card limit
        val totalExistingCardLimit = input.existingCards.fold(ZERO) { a, b -> a.add(b.creditLimit, MC) }
        val threePercentExistingCardLimits = totalExistingCardLimit
            .multiply(BigDecimal.valueOf(0.03), MC)
            .setScale(DISPLAY_SCALE, RoundingMode.HALF_UP)

        // Option 2: 5% of total last one-year average outstanding
        val totalAverageOutstanding = input.existingCards.fold(ZERO) { a, b -> a.add(b.lastOneYearAverageOutstanding, MC) }
        val fivePercentAverageOutstanding = totalAverageOutstanding
            .multiply(BigDecimal.valueOf(0.05), MC)
            .setScale(DISPLAY_SCALE, RoundingMode.HALF_UP)

        // Use whichever is HIGHER, exactly as the PPG states
        val higherExistingCardLiability = if (threePercentExistingCardLimits >= fivePercentAverageOutstanding) {
            threePercentExistingCardLimits
        } else {
            fivePercentAverageOutstanding
        }

        // D. Total Monthly Obligations
        val totalMonthlyObligations = existingLoanEmisSum
            .add(fivePercentProposedCardLimit)
            .add(higherExistingCardLiability)

        // E. DBR = Total Monthly Obligations / Monthly Net Salary × 100
        val dbrValue = if (input.monthlyNetSalary > ZERO) {
            totalMonthlyObligations
                .divide(input.monthlyNetSalary, MC)
                .multiply(BigDecimal.valueOf(100))
                .setScale(DISPLAY_SCALE, RoundingMode.HALF_UP)
        } else {
            ZERO
        }

        // F. Determine maximum allowed DVR based on income grid
        val maximumAllowedDvr = getMaximumAllowedDbr(input.monthlyNetSalary)

        // G. Check eligibility
        val isEligible = dbrValue.toDouble() <= maximumAllowedDvr

        return Result(
            dbrValue = dbrValue,
            totalMonthlyObligations = totalMonthlyObligations,
            totalNetSalary = input.monthlyNetSalary,
            existingLoanEmis = existingLoanEmisSum,
            fivePercentProposedCardLimit = fivePercentProposedCardLimit,
            threePercentExistingCardLimits = threePercentExistingCardLimits,
            fivePercentAverageOutstanding = fivePercentAverageOutstanding,
            higherExistingCardLiability = higherExistingCardLiability,
            maximumAllowedDvr = maximumAllowedDvr,
            isEligible = isEligible
        )
    }

    /** Get maximum allowed DBR based on income grid */
    private fun getMaximumAllowedDbr(monthlyNetSalary: BigDecimal): Double {
        val salary = monthlyNetSalary.toLong()

        return when (salary) {
            in 20000..29999 -> 35.0
            in 30000..49999 -> 45.0
            in 50000..74999 -> 50.0
            in 75000..99999 -> 55.0
            in 100000..199999 -> 60.0
            else -> 65.0 // 200000+
        }
    }

    /** Apply businessman/landlord 50% cap */
    fun applyBusinessmanLandlordCap(
        baseDbr: Double,
        customerType: CreditCardCustomerType
    ): Double {
        return when (customerType) {
            CreditCardCustomerType.Businessman,
            CreditCardCustomerType.Landlord -> baseDbr.coerceIn(0.0, 50.0)
            else -> baseDbr
        }
    }

    /** Apply own house relaxation up to 5% */
    fun applyOwnHouseRelaxation(
        baseDbr: Double,
        ownsOwnHouse: Boolean,
        hasHomeLoan: Boolean
    ): Double {
        if (ownsOwnHouse && !hasHomeLoan) {
            val relaxed = baseDbr + 5.0
            return relaxed.coerceIn(0.0, 100.0)
        }
        return baseDbr
    }

    /** Determine customer type influence on DBR */
    fun shouldSkipDbrCalculation(
        customerType: CreditCardCustomerType
    ): String? {
        return when (customerType) {
            CreditCardCustomerType.GridCustomer -> {
                "DVR/DBR calculation is not applicable for Grid customers according to the Credit Card PPG"
            }
            CreditCardCustomerType.BankAsiaStaffCard -> {
                "DBR calculation is not applicable according to the Credit Card PPG for Bank Asia Staff Card"
            }
            else -> null
        }
    }

    /** Customer types for DBR influence */
    enum class CreditCardCustomerType {
        Salaried,
        Businessman,
        Landlord,
        Other,
        GridCustomer,
        BankAsiaStaffCard
    }
}