package com.bankasia.smartcalc.domain.ppg

/**
 * Bank Asia Credit Card PPG (February 2025) Rules Documentation.
 * This file documents the PPG rules with source traceability.
 *
 * Source: Bank Asia Credit Card Product Program Guideline, February 2025
 *
 * IMPORTANT: This is the DBR calculation methodology.
 */
object CreditCardPpgRules {

    /**
     * DBR Formula from Credit Card PPG
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
     */

    /**
     * Minimum Net Monthly Income Requirements
     *
     * - Govt/Semi-Govt/Autonomous Bodies / Women Credit Card: BDT 22,000
     * - A Type/Corporate/Payroll: BDT 25,000
     * - B Type: BDT 30,000
     * - C Type/Non-CAT: BDT 35,000
     * - RMG: BDT 35,000
     * - Landlord/Businessmen: BDT 50,000
     */

    /**
     * DBR Slabs by Monthly Income
     *
     * BDT 20,000 – 29,999 → 35%
     * BDT 30,000 – 49,999 → 45%
     * BDT 50,000 – 74,999 → 50%
     * BDT 75,000 – 99,999 → 55%
     * BDT 100,000 – 199,999 → 60%
     * BDT 200,000+ → 65%
     */

    /**
     * Special Rules
     *
     * - DBR will not cross 50% for businessmen and landlords (capped at 50%)
     * - Up to 5% relaxation may apply for own house/family-owned house
     * - DBR is not considered for Grid customers
     * - Bank Asia Staff Card segment does not require DBR calculation
     */

    /**
     * Existing Loan EMIs
     *
     * - All existing installment loans from any bank/financial institution
     * - Monthly EMI amount summed for calculation
     * - Institution name optional for reference
     */

    /**
     * Proposed Credit Card Treatment
     *
     * - 5% of proposed credit-card limit included in DBR numerator
     * - Proposed limit is the amount being applied for
     */

    /**
     * Existing Credit Card Treatment
     *
     * Two options are calculated, HIGHER value is used:
     *
     * Option 1: 3% of total existing credit-card limit
     *   - Sum of all existing credit card limits × 3%
     *
     * Option 2: 5% of last one-year average outstanding
     *   - Sum of average outstanding for each card over last 12 months × 5%
     *
     * The higher of these two options is used in the DBR calculation.
     */

    /**
     * Customer Type Treatment
     *
     * Salaried:
     * - Uses standard income grid
     * - No special caps
     *
     * Businessman / Landlord:
     * - DBR capped at 50% maximum
     * - Still eligible for up to 5% own house relaxation
     *
     * Grid Customer:
     * - DBR/DBR calculation NOT applicable
     * - Clearly display: "DVR/DBR calculation is not applicable for Grid customers"
     *
     * Bank Asia Staff Card:
     * - DBR calculation NOT applicable
     * - Clearly display: "DBR calculation is not applicable according to PPG"
     */

    /**
     * Income Components
     *
     * Monthly Net Salary:
     * - Take-home pay after all deductions
     * - Primary income component for salaried individuals
     *
     * Other income may be included per PPG methodology where applicable.
     */
}