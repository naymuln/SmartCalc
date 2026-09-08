package com.bankasia.smartcalc.domain.util

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Pure Kotlin utility for Indian numbering system formatting (Lakhs and Crores).
 *
 * Rules:
 * - The rightmost 3 digits are grouped together.
 * - Remaining digits to the left are grouped in pairs of 2.
 * Examples:
 *   1000 -> 1,000
 *   10000 -> 10,000
 *   100000 -> 1,00,000
 *   1000000 -> 10,00,000
 *   10000000 -> 1,00,00,000
 */
object IndianNumberFormatter {

    /**
     * Format a BigDecimal with Indian grouping.
     * Keeps two decimal places unless .00, in which case decimal part is shown or omitted based on keepZeroDecimals.
     */
    fun formatIndianNumber(value: BigDecimal, keepZeroDecimals: Boolean = false): String {
        val scale = 2
        val rounded = value.setScale(scale, RoundingMode.HALF_UP)
        val isNegative = rounded < BigDecimal.ZERO
        val abs = rounded.abs()
        val intPart = abs.setScale(0, RoundingMode.DOWN).toBigInteger()
        val decPart = abs.subtract(intPart.toBigDecimal()).setScale(scale, RoundingMode.HALF_UP)

        val intFormatted = formatIndianIntegerString(intPart.toString())
        val decStr = decPart.toString().substringAfter('.')

        val result = if (keepZeroDecimals || decStr != "00") {
            "$intFormatted.$decStr"
        } else {
            intFormatted
        }

        return if (isNegative) "-$result" else result
    }

    /**
     * Format a BigDecimal as Bangladeshi Taka with Indian comma grouping.
     */
    fun formatIndianCurrency(value: BigDecimal): String {
        return "৳ ${formatIndianNumber(value, keepZeroDecimals = false)}"
    }

    /**
     * Helper to format an integer digit string using Indian numbering rules.
     */
    fun formatIndianIntegerString(intStr: String): String {
        if (intStr.length <= 3) return intStr

        val result = StringBuilder()
        val len = intStr.length
        for (i in 0 until len) {
            result.append(intStr[i])
            val digitsRemaining = (len - 1) - i
            // Comma after this digit if >= 3 digits remain, and (digitsRemaining - 3) is even, and digitsRemaining > 0
            if (digitsRemaining >= 3 && (digitsRemaining - 3) % 2 == 0 && digitsRemaining > 0) {
                result.append(',')
            }
        }
        return result.toString()
    }

    /**
     * Formats an arbitrary string that may contain integer and decimal parts with Indian commas.
     */
    fun formatIndianString(raw: String): String {
        if (raw.isBlank()) return ""
        val clean = raw.replace(",", "").trim()
        val isNegative = clean.startsWith('-')
        val unsigned = if (isNegative) clean.substring(1) else clean

        val parts = unsigned.split('.', limit = 2)
        val intPart = parts[0]
        val formattedInt = formatIndianIntegerString(intPart)

        val formatted = if (parts.size > 1) {
            "$formattedInt.${parts[1]}"
        } else if (clean.endsWith('.')) {
            "$formattedInt."
        } else {
            formattedInt
        }

        return if (isNegative) "-$formatted" else formatted
    }
}
