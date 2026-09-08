package com.bankasia.smartcalc.domain.ppg

/**
 * Bank Asia Personal Loan PPD-2026 Rules Documentation.
 * This file documents the PPG/PPD rules with source traceability.
 *
 * Source: Bank Asia Personal Loan Product Policy Details (PPD-2026)
 *
 * IMPORTANT: This is the capacity-to-pay (DVR) calculation methodology.
 */
object PersonalLoanPpdRules {

    /**
     * DVR Formula from PPD (Capacity to Pay)
     *
     * Numerator (Total Monthly Obligations):
     * 1. EMIs of all existing installment-based loans (Bank Asia + other banks/FIs)
     * 2. Average monthly interest of CC/Overdraft loans
     * 3. 3% of credit-card limit
     * 4. Proposed Personal Loan installment
     *
     * Denominator (Total Considered Income):
     * - Sum of all considered income components
     *
     * DVR = (Numerator / Denominator) × 100
     */

    /**
     * Income Assessment Rules
     *
     * Salary Income:
     * - Net income = Gross income - Tax deductions - Other deductions
     *
     * Variable Income (overtime, commission, incentives):
     * - Assessed using average of last 3-6 months
     *
     * House Rent Income:
     * - Only for property within Bank Asia branch command area
     * - Only for eligible properties (house/shop, not semi-pacca/tin-shed)
     * - 80% of rent received can be considered
     *
     * Pension:
     * - Fully considered if properly substantiated
     *
     * Interest from FDR/Savings securities:
     * - Fully considered if documented
     *
     * Business/Professional Income:
     * - Only included per specific PPD income assessment methodology
     * - Not automatically 100% of declared amount
     */

    /**
     * DVR Slabs for Category A & B Customers
     *
     * BDT 20,000 – 34,999 → 35%
     * BDT 35,000 – 54,999 → 40%
     * BDT 55,000 – 69,999 → 50%
     * BDT 70,000 – 99,999 → 55%
     * BDT 100,000 – 149,999 → 60%
     * BDT 150,000+ → 65%
     */

    /**
     * DVR Slabs for Bank Asia Employee Customers
     *
     * BDT 20,000 – 29,999 → 35%
     * BDT 30,000 – 49,999 → 40%
     * BDT 50,000 – 69,999 → 50%
     * BDT 70,000 – 99,999 → 55%
     * BDT 100,000 – 149,999 → 60%
     * BDT 150,000+ → 65%
     */

    /**
     * Home Ownership Relaxation (+10%)
     *
     * - Applies if customer owns their own house OR father's own house
     * - Additional 10% DVR allowed in the respective income grid
     * - DOES NOT apply if customer has a home-loan facility for that house
     *
     * Example:
     * Base DVR limit = 50%
     * Own house relaxation = +10%
     * Final permitted DVR = 60%
     */

    /**
     * Joint Applicant Rules
     *
     * - Only spouses are allowed as joint borrowers
     * - Spouse income assessed using same methodology
     * - Combined income = Primary + Spouse considered income
     * - Total liabilities include both applicants' obligations
     */

    /**
     * Special Senior Professional Proposition
     *
     * - For certain senior professionals / high-level government employees / bankers / university teachers
     * - May calculate 10% more DBR than the proposed grid
     * - Must be explicitly enabled, not default behavior
     */

    /**
     * Rental Income Rules
     *
     * Property within branch command area:
     * - Eligible house/shop: 80% of rent considered
     * - Semi-pacca/Tin shed: NOT considered
     *
     * Property outside branch command area:
     * - NOT considered (0%)
     */

    /**
     * Existing Loan Treatment
     *
     * - All existing installment-based loans from Bank Asia and other banks/FIs
     * - EMIs included in DVR numerator
     * - Each loan counted separately
     */

    /**
     * Credit Card Treatment
     *
     * - 3% of total credit-card limit included in DVR numerator
     * - CC/OD monthly interest also included
     */

    /**
     * Proposed Loan Treatment
     *
     * - Proposed personal loan EMI included in numerator
     * - EMI calculated using standard reducing balance formula
     */

    /**
     * Customer Categories
     *
     * 1. Category A/B Target Customers
     * 2. Bank Asia Employee / Category C
     *
     * Separate grids apply; do not mix them.
     */
}