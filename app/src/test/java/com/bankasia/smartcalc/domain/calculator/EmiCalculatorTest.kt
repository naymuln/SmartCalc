package com.bankasia.smartcalc.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class EmiCalculatorTest {

    @Test
    fun `EMI calculation with 0% interest`() {
        val input = EmiCalculator.Input(
            principal = BigDecimal("1000000"),
            annualInterestRate = BigDecimal.ZERO,
            tenure = 60,
            tenureUnit = EmiCalculator.TenureUnit.MONTHS
        )

        val result = EmiCalculator.calculate(input)

        assertEquals(BigDecimal("1000000"), result.principal)
        assertEquals(BigDecimal.ZERO, result.totalInterest)
        assertEquals(BigDecimal("1000000"), result.totalRepayment)
        assertEquals(BigDecimal("16666.67"), result.monthlyEmi)
    }

    @Test
    fun `EMI calculation with 9% annual interest for 5 years`() {
        val input = EmiCalculator.Input(
            principal = BigDecimal("1000000"),
            annualInterestRate = BigDecimal("9.0"),
            tenure = 5,
            tenureUnit = EmiCalculator.TenureUnit.YEARS
        )

        val result = EmiCalculator.calculate(input)

        assertEquals(BigDecimal("1000000"), result.principal)
        assertTrue(result.totalInterest > BigDecimal.ZERO)
        assertTrue(result.totalRepayment > BigDecimal("1000000"))
        assertTrue(result.monthlyEmi > BigDecimal("0"))
    }

    @Test
    fun `EMI calculation with 12% annual interest for 2 years`() {
        val input = EmiCalculator.Input(
            principal = BigDecimal("500000"),
            annualInterestRate = BigDecimal("12.0"),
            tenure = 2,
            tenureUnit = EmiCalculator.TenureUnit.YEARS
        )

        val result = EmiCalculator.calculate(input)

        assertEquals(BigDecimal("500000"), result.principal)
        assertTrue(result.totalInterest > BigDecimal.ZERO)
        assertTrue(result.totalRepayment > BigDecimal("500000"))
    }

    @Test
    fun `Negative inputs should throw exception`() {
        val input = EmiCalculator.Input(
            principal = BigDecimal("-1000000"),
            annualInterestRate = BigDecimal("9.0"),
            tenure = 5,
            tenureUnit = EmiCalculator.TenureUnit.YEARS
        )

        try {
            EmiCalculator.calculate(input)
            assertTrue("Should have thrown exception for negative principal", false)
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Principal") == true)
        }
    }

    @Test
    fun `Zero tenure should throw exception`() {
        val input = EmiCalculator.Input(
            principal = BigDecimal("1000000"),
            annualInterestRate = BigDecimal("9.0"),
            tenure = 0,
            tenureUnit = EmiCalculator.TenureUnit.YEARS
        )

        try {
            EmiCalculator.calculate(input)
            assertTrue("Should have thrown exception for zero tenure", false)
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Tenure") == true)
        }
    }

    @Test
    fun `Large loan with decimal interest rate`() {
        val input = EmiCalculator.Input(
            principal = BigDecimal("2500000.50"),
            annualInterestRate = BigDecimal("8.75"),
            tenure = 30,
            tenureUnit = EmiCalculator.TenureUnit.YEARS
        )

        val result = EmiCalculator.calculate(input)

        assertEquals(BigDecimal("2500000.50"), result.principal)
        assertTrue(result.totalInterest > BigDecimal.ZERO)
        assertTrue(result.monthlyEmi > BigDecimal("0"))
    }
}