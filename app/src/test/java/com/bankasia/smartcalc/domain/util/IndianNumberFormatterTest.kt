package com.bankasia.smartcalc.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class IndianNumberFormatterTest {

    @Test
    fun `formatIndianIntegerString tests`() {
        assertEquals("0", IndianNumberFormatter.formatIndianIntegerString("0"))
        assertEquals("50", IndianNumberFormatter.formatIndianIntegerString("50"))
        assertEquals("999", IndianNumberFormatter.formatIndianIntegerString("999"))
        assertEquals("1,000", IndianNumberFormatter.formatIndianIntegerString("1000"))
        assertEquals("10,000", IndianNumberFormatter.formatIndianIntegerString("10000"))
        assertEquals("1,00,000", IndianNumberFormatter.formatIndianIntegerString("100000"))
        assertEquals("10,00,000", IndianNumberFormatter.formatIndianIntegerString("1000000"))
        assertEquals("1,00,00,000", IndianNumberFormatter.formatIndianIntegerString("10000000"))
        assertEquals("12,34,56,789", IndianNumberFormatter.formatIndianIntegerString("123456789"))
    }

    @Test
    fun `formatIndianNumber BigDecimal tests`() {
        assertEquals("1,000", IndianNumberFormatter.formatIndianNumber(BigDecimal("1000")))
        assertEquals("1,00,000", IndianNumberFormatter.formatIndianNumber(BigDecimal("100000")))
        assertEquals("10,00,000", IndianNumberFormatter.formatIndianNumber(BigDecimal("1000000")))
        assertEquals("1,00,00,000", IndianNumberFormatter.formatIndianNumber(BigDecimal("10000000")))
        assertEquals("12,34,567.89", IndianNumberFormatter.formatIndianNumber(BigDecimal("1234567.89")))
        assertEquals("-1,00,000", IndianNumberFormatter.formatIndianNumber(BigDecimal("-100000")))
    }

    @Test
    fun `formatIndianCurrency tests`() {
        assertEquals("৳ 1,00,000", IndianNumberFormatter.formatIndianCurrency(BigDecimal("100000")))
        assertEquals("৳ 10,00,000", IndianNumberFormatter.formatIndianCurrency(BigDecimal("1000000")))
        assertEquals("৳ 1,00,00,000", IndianNumberFormatter.formatIndianCurrency(BigDecimal("10000000")))
    }

    @Test
    fun `formatIndianString tests`() {
        assertEquals("1,00,000", IndianNumberFormatter.formatIndianString("100000"))
        assertEquals("1,00,000.5", IndianNumberFormatter.formatIndianString("100000.5"))
        assertEquals("10,00,000.", IndianNumberFormatter.formatIndianString("1000000."))
        assertEquals("-1,00,000", IndianNumberFormatter.formatIndianString("-100000"))
    }
}
