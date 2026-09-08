package com.bankasia.smartcalc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import java.math.BigDecimal
import java.math.RoundingMode

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    inverseOnSurface = InverseOnSurface,
    inverseSurface = InverseSurface,
    inversePrimary = InversePrimary,
    scrim = Scrim
)

@Composable
fun SmartCalcTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}

/**
 * Format a BigDecimal as Bangladeshi Taka currency.
 * Uses comma-separated formatting with ৳ symbol.
 */
fun formatCurrency(value: BigDecimal): String {
    val scale = 2
    val rounded = value.setScale(scale, RoundingMode.HALF_UP)
    val isNegative = rounded < BigDecimal.ZERO
    val abs = rounded.abs()
    val intPart = abs.setScale(0, RoundingMode.DOWN).toBigInteger()
    val decPart = abs.subtract(intPart.toBigDecimal()).setScale(scale, RoundingMode.HALF_UP)

    val intStr = intPart.toString()
    val result = StringBuilder()
    var count = 0
    for (i in intStr.length - 1 downTo 0) {
        if (count > 0 && count % 3 == 0) result.insert(0, ',')
        result.insert(0, intStr[i])
        count++
    }

    val decStr = decPart.toString().substringAfter('.')
    if (decStr != "00") {
        result.append('.').append(decStr)
    }

    val formatted = if (isNegative) "-$result" else result.toString()
    return "৳ $formatted"
}
