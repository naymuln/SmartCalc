package com.bankasia.smartcalc.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class PersonalLoanDvrCalculatorTest {

    @Test
    fun `Personal Loan DVR with existing loans only`() {
        val result = PersonalLoanDvrCalculator.calculateDvr(
            monthlyIncome = BigDecimal("50000"),
            existingLoanEmis = listOf(BigDecimal("10000"), BigDecimal("5000")),
            ccAverageMonthlyInterest = BigDecimal.ZERO,
            totalCreditCardLimit = BigDecimal.ZERO,
            proposedLoanEmi = BigDecimal.ZERO,
            ownsOwnHouse = false,
            hasHomeLoan = false,
            customerCategory = PersonalLoanDvrCalculator.CustomerCategory.CategoryAB
        )

        // Obligations: 10000 + 5000 = 15000
        // Income: 50000
        // DVR = 15000/50000 * 100 = 30%
        assertEquals(BigDecimal("30.00"), result.dvrValue)
        assertEquals(40.0, result.permittedDvr, 0.01) // Category A/B 35k-54k = 40%
    }

    @Test
    fun `Personal Loan DVR with credit card liability`() {
        val result = PersonalLoanDvrCalculator.calculateDvr(
            monthlyIncome = BigDecimal("100000"),
            existingLoanEmis = listOf(BigDecimal("20000")),
            ccAverageMonthlyInterest = BigDecimal.ZERO,
            totalCreditCardLimit = BigDecimal("100000"), // 3% = 3000
            proposedLoanEmi = BigDecimal.ZERO,
            ownsOwnHouse = false,
            hasHomeLoan = false,
            customerCategory = PersonalLoanDvrCalculator.CustomerCategory.CategoryAB
        )

        // Obligations: 20000 + 3000 = 23000
        // Income: 100000
        // DVR = 23000/100000 * 100 = 23%
        assertEquals(BigDecimal("23.00"), result.dvrValue)
        assertEquals(BigDecimal("3000.00"), result.creditCardThreePercentLiability)
    }

    @Test
    fun `Personal Loan DVR with proposed loan`() {
        val proposedEmi = PersonalLoanDvrCalculator.calculateProposedLoanEmi(
            principal = BigDecimal("500000"),
            annualInterestRate = BigDecimal("10.0"),
            tenureMonths = 60
        )

        val result = PersonalLoanDvrCalculator.calculateDvr(
            monthlyIncome = BigDecimal("50000"),
            existingLoanEmis = listOf(BigDecimal("10000")),
            ccAverageMonthlyInterest = BigDecimal.ZERO,
            totalCreditCardLimit = BigDecimal.ZERO,
            proposedLoanEmi = proposedEmi,
            ownsOwnHouse = false,
            hasHomeLoan = false,
            customerCategory = PersonalLoanDvrCalculator.CustomerCategory.CategoryAB
        )

        assertTrue(result.proposedLoanEmi > BigDecimal.ZERO)
        assertTrue(result.dvrValue > BigDecimal("20.00")) // More than 10000/50000 = 20%
    }

    @Test
    fun `Personal Loan DVR own house relaxation`() {
        val resultWithoutRelaxation = PersonalLoanDvrCalculator.calculateDvr(
            monthlyIncome = BigDecimal("80000"),
            existingLoanEmis = listOf(BigDecimal("30000")),
            ccAverageMonthlyInterest = BigDecimal.ZERO,
            totalCreditCardLimit = BigDecimal.ZERO,
            proposedLoanEmi = BigDecimal.ZERO,
            ownsOwnHouse = false,
            hasHomeLoan = false,
            customerCategory = PersonalLoanDvrCalculator.CustomerCategory.CategoryAB
        )

        val resultWithRelaxation = PersonalLoanDvrCalculator.calculateDvr(
            monthlyIncome = BigDecimal("80000"),
            existingLoanEmis = listOf(BigDecimal("30000")),
            ccAverageMonthlyInterest = BigDecimal.ZERO,
            totalCreditCardLimit = BigDecimal.ZERO,
            proposedLoanEmi = BigDecimal.ZERO,
            ownsOwnHouse = true,
            hasHomeLoan = false,
            customerCategory = PersonalLoanDvrCalculator.CustomerCategory.CategoryAB
        )

        assertEquals(55.0, resultWithoutRelaxation.permittedDvr, 0.01) // 70k-99k = 55%
        assertEquals(65.0, resultWithRelaxation.finalPermittedDvr, 0.01) // 55% + 10% = 65%
    }

    @Test
    fun `Personal Loan DVR own house with home loan restriction`() {
        val resultWithHomeLoan = PersonalLoanDvrCalculator.calculateDvr(
            monthlyIncome = BigDecimal("80000"),
            existingLoanEmis = listOf(BigDecimal("30000")),
            ccAverageMonthlyInterest = BigDecimal.ZERO,
            totalCreditCardLimit = BigDecimal.ZERO,
            proposedLoanEmi = BigDecimal.ZERO,
            ownsOwnHouse = true,
            hasHomeLoan = true,
            customerCategory = PersonalLoanDvrCalculator.CustomerCategory.CategoryAB
        )

        assertEquals(55.0, resultWithHomeLoan.finalPermittedDvr, 0.01) // No +10% due to home loan
    }

    @Test
    fun `Personal Loan DVR Bank Asia Employee grid`() {
        val resultCategoryAB = PersonalLoanDvrCalculator.calculateDvr(
            monthlyIncome = BigDecimal("35000"),
            existingLoanEmis = listOf(),
            ccAverageMonthlyInterest = BigDecimal.ZERO,
            totalCreditCardLimit = BigDecimal.ZERO,
            proposedLoanEmi = BigDecimal.ZERO,
            ownsOwnHouse = false,
            hasHomeLoan = false,
            customerCategory = PersonalLoanDvrCalculator.CustomerCategory.CategoryAB
        )

        val resultEmployee = PersonalLoanDvrCalculator.calculateDvr(
            monthlyIncome = BigDecimal("35000"),
            existingLoanEmis = listOf(),
            ccAverageMonthlyInterest = BigDecimal.ZERO,
            totalCreditCardLimit = BigDecimal.ZERO,
            proposedLoanEmi = BigDecimal.ZERO,
            ownsOwnHouse = false,
            hasHomeLoan = false,
            customerCategory = PersonalLoanDvrCalculator.CustomerCategory.Employee
        )

        assertEquals(40.0, resultCategoryAB.permittedDvr, 0.01) // Cat A/B 35k-54k = 40%
        assertEquals(40.0, resultEmployee.permittedDvr, 0.01) // Employee 30k-49k = 40%
    }

    @Test
    fun `Personal Loan DVR boundary values`() {
        // Exactly at slab boundary
        val result = PersonalLoanDvrCalculator.calculateDvr(
            monthlyIncome = BigDecimal("35000"),
            existingLoanEmis = listOf(BigDecimal("10000")),
            ccAverageMonthlyInterest = BigDecimal.ZERO,
            totalCreditCardLimit = BigDecimal.ZERO,
            proposedLoanEmi = BigDecimal.ZERO,
            ownsOwnHouse = false,
            hasHomeLoan = false,
            customerCategory = PersonalLoanDvrCalculator.CustomerCategory.CategoryAB
        )

        assertEquals(40.0, result.permittedDvr, 0.01)
    }

    @Test
    fun `Personal Loan DVR zero liabilities`() {
        val result = PersonalLoanDvrCalculator.calculateDvr(
            monthlyIncome = BigDecimal("100000"),
            existingLoanEmis = listOf(),
            ccAverageMonthlyInterest = BigDecimal.ZERO,
            totalCreditCardLimit = BigDecimal.ZERO,
            proposedLoanEmi = BigDecimal.ZERO,
            ownsOwnHouse = false,
            hasHomeLoan = false,
            customerCategory = PersonalLoanDvrCalculator.CustomerCategory.CategoryAB
        )

        assertEquals(BigDecimal("0.00"), result.dvrValue)
    }

    @Test
    fun `Proposed loan EMI calculation`() {
        val emi = PersonalLoanDvrCalculator.calculateProposedLoanEmi(
            principal = BigDecimal("1000000"),
            annualInterestRate = BigDecimal("10.0"),
            tenureMonths = 60
        )

        // Expected: ~21,247 BDT
        assertTrue(emi > BigDecimal("20000"))
        assertTrue(emi < BigDecimal("22000"))
    }

    @Test
    fun `Credit card liability 3%`() {
        val liability = PersonalLoanDvrCalculator.calculateCardLiability(BigDecimal("500000"))
        assertEquals(BigDecimal("15000.00"), liability)
    }
}