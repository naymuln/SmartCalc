package com.bankasia.smartcalc.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.bankasia.smartcalc.domain.util.IndianNumberFormatter

/**
 * VisualTransformation that dynamically inserts Indian commas into numeric text fields as the user types,
 * while maintaining exact cursor position mapping and preventing out-of-bounds crashes.
 */
class IndianNumberVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        if (original.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        // Clean any existing commas if pasted
        val clean = original.replace(",", "")
        if (clean.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val isNegative = clean.startsWith('-')
        val unsigned = if (isNegative) clean.substring(1) else clean

        val dotIndex = unsigned.indexOf('.')
        val intPart = if (dotIndex != -1) unsigned.substring(0, dotIndex) else unsigned
        val decPart = if (dotIndex != -1) unsigned.substring(dotIndex) else ""

        // Build transformed string
        val transformed = StringBuilder()
        if (isNegative) transformed.append('-')

        val intLen = intPart.length
        for (i in 0 until intLen) {
            transformed.append(intPart[i])
            val digitsRemaining = (intLen - 1) - i
            if (digitsRemaining >= 3 && (digitsRemaining - 3) % 2 == 0 && digitsRemaining > 0) {
                transformed.append(',')
            }
        }
        transformed.append(decPart)

        val transformedString = transformed.toString()

        // Build precise bidirectional offset mapping arrays
        val originalToTransformed = IntArray(original.length + 1)
        val transformedToOriginal = IntArray(transformedString.length + 1)

        var origIdx = 0
        var transIdx = 0

        while (origIdx < original.length && transIdx < transformedString.length) {
            originalToTransformed[origIdx] = transIdx
            transformedToOriginal[transIdx] = origIdx

            val origChar = original[origIdx]
            val transChar = transformedString[transIdx]

            if (transChar == ',' && origChar != ',') {
                // Comma inserted in transformed text
                transIdx++
            } else {
                origIdx++
                transIdx++
            }
        }

        // Fill remaining end indices
        while (origIdx <= original.length) {
            originalToTransformed[origIdx] = transformedString.length
            origIdx++
        }
        while (transIdx <= transformedString.length) {
            transformedToOriginal[transIdx] = original.length
            transIdx++
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, originalToTransformed.lastIndex)
                return originalToTransformed[clamped]
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, transformedToOriginal.lastIndex)
                return transformedToOriginal[clamped]
            }
        }

        return TransformedText(AnnotatedString(transformedString), offsetMapping)
    }
}
