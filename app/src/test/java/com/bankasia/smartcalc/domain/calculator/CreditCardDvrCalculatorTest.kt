package com.bankasia.smartcalc.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class CreditCardDvrCalculatorTest {

    @Test
    fun `Credit Card DVR with no existing loans/cards`() {
        val input = CreditCardDvrCalculator.Input(
            monthlyNetSalary = BigDecimal("30000"),
            existingLoanEmis = listOf(),
            proposedCardLimit = BigDecimal("50000"),
            existingCards = listOf()
        )

        val result = CreditCardDvrCalculator.calculateDbr(input)

        assertEquals(BigDecimal("8.33"), result.dbrValue)
        assertEquals(45.0, result.maximumAllowedDvr, 0.01)
        assertTrue(result.isEligible)
    }

    @Test
    fun `Credit Card DVR with existing loan EMI`() {
        val input = CreditCardDvrCalculator.Input(
            monthlyNetSalary = BigDecimal("50000"),
            existingLoanEmis = listOf(CreditCardDvrCalculator.ExistingLoanInput("Bank A", BigDecimal("15000"))),
            proposedCardLimit = BigDecimal("100000"),
            existingCards = listOf()
        )

        val result = CreditCardDvrCalculator.calculateDbr(input)

        assertEquals(BigDecimal("40.00"), result.dbrValue)
        assertEquals(50.0, result.maximumAllowedDvr, 0.01)
        assertTrue(result.isEligible)
    }

    @Test
    fun `Credit Card DVR with existing card limit higher than avg outstanding`() {
        val input = CreditCardDvrCalculator.Input(
            monthlyNetSalary = BigDecimal("40000"),
            existingLoanEmis = listOf(),
            proposedCardLimit = BigDecimal("0"),
            existingCards = listOf(
                CreditCardDvrCalculator.ExistingCardInput(BigDecimal("200000"), BigDecimal("100000"))
            )
        )

        val result = CreditCardDvrCalculator.calculateDbr(input)

        assertEquals(BigDecimal("15.00"), result.dbrValue)
        assertEquals(45.0, result.maximumAllowedDvr, 0.01)
        assertTrue(result.isEligible)
    }

    @Test
    fun `Credit Card DVR with average outstanding higher than limit`() {
        val input = CreditCardDvrCalculator.Input(
            monthlyNetSalary = BigDecimal("40000"),
            existingLoanEmis = listOf(),
            proposedCardLimit = BigDecimal("0"),
            existingCards = listOf(
                CreditCardDvrCalculator.ExistingCardInput(BigDecimal("50000"), BigDecimal("500000"))
            )
        )

        val result = CreditCardDvrCalculator.calculateDbr(input)

        assertEquals(BigDecimal("62.50"), result.dbrValue)
        assertEquals(45.0, result.maximumAllowedDvr, 0.01)
        assertTrue(result.isEligible)
    }

    @Test
    fun `Credit Card DVR multiple cards`() {
        val input = CreditCardDvrCalculator.Input(
            monthlyNetSalary = BigDecimal("60000"),
            existingLoanEmis = listOf(),
            proposedCardLimit = BigDecimal("0"),
            existingCards = listOf(
                CreditCardDvrCalculator.ExistingCardInput(BigDecimal("100000"), BigDecimal("50000")),
                CreditCardDvrCalculator.ExistingCardInput(BigDecimal("150000"), BigDecimal("80000"))
            )
        )

        val result = CreditCardDvrCalculator.calculateDbr(input)

        assertEquals(BigDecimal("12.50"), result.dbrValue)
        assertEquals(55.0, result.maximumAllowedDvr, 0.01)
        assertTrue(result.isEligible)
    }

    @Test
    fun `Credit Card DVR businessman cap at 50%`() {
        val input = CreditCardDvrCalculator.Input(
            monthlyNetSalary = BigDecimal("50000"),
            existingLoanEmis = listOf(),
            proposedCardLimit = BigDecimal("500000"),
            existingCards = listOf()
        )

        val result = CreditCardDvrCalculator.calculateDbr(input)

        assertEquals(BigDecimal("50.00"), result.dbrValue)
        assertEquals(50.0, result.maximumAllowedDvr, 0.01)
        assertTrue(result.isEligible)
    }

    @Test
    fun `Credit Card DVR own house relaxation`() {
        val input = CreditCardDvrCalculator.Input(
            monthlyNetSalary = BigDecimal("40000"),
            existingLoanEmis = listOf(),
            proposedCardLimit = BigDecimal("100000"),
            existingCards = listOf()
        )

        val result = CreditCardDvrCalculator.calculateDbr(input)

        assertEquals(BigDecimal("17.50"), result.dbrValue)
        assertEquals(50.0, result.maximumAllowedDvr, 0.01)
        assertTrue(result.isEligible)
    }

    @Test
    fun `Credit Card DVR grid customer`() {
        val input = CreditCardDvrCalculator.Input(
            monthlyNetSalary = BigDecimal("100000"),
            existingLoanEmis = listOf(),
            proposedCardLimit = BigDecimal("500000"),
            existingCards = listOf()
        )

        val result = CreditCardDvrCalculator.calculateDbr(input)

        assertEquals(BigDecimal("25.00"), result.dbrValue)
        assertEquals(60.0, result.maximumAllowedDvr, 0.01)
    }

    @Test
    fun `Credit Card DVR Bank Asia Staff Card`() {
        val input = CreditCardDvrCalculator.Input(
            monthlyNetSalary = BigDecimal("50000"),
            existingLoanEmis = listOf(),
            proposedCardLimit = BigDecimal("100000"),
            existingCards = listOf()
        )

        val result = CreditCardDvrCalculator.calculateDbr(input)

        assertEquals(BigDecimal("5.00"), result.dbrValue)
        assertEquals(50.0, result.maximumAllowedDvr, 0.01)
    }

    @Test
    fun `Credit Card DVR income slab boundaries`() {
        // Test boundary: 29999 (should be 35%)
        val input = CreditCardDvrCalculator.Input(
            monthlyNetSalary = BigDecimal("29999"),
            existingLoanEmis = listOf(),
            proposedCardLimit = BigDecimal("0"),
            existingCards = listOf()
        )

        val result = CreditCardDvrCalculator.calculateDbr(input)
        assertEquals(35.0, result.maximumAllowedDvr, 0.01)

        // Test boundary: 30000 (should be 45%)
        val input2 = CreditCardDvrCalculator.Input(
            monthlyNetSalary = BigDecimal("30000"),
            existingLoanEmis = listOf(),
            proposedCardLimit = BigDecimal("0"),
            existingCards = listOf()
        )

        val result2 = CreditCardDvrCalculator.calculateDbr(input2)
        assertEquals(45.0, result2.maximumAllowedDvr, 0.01)
    }
}