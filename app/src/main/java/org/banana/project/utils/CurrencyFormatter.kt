package org.banana.project.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Utility for formatting monetary amounts using Colombian Peso (COP) conventions.
 * Output format: $#,##0.00 (e.g., $45.000,00)
 */
object CurrencyFormatter {
    private val colSymbols = DecimalFormatSymbols(Locale.forLanguageTag("es-CO")).apply {
        currencySymbol = "$"
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    private val copFormat = DecimalFormat("$#,##0.00", colSymbols)

    /**
     * Formats a monetary amount into a Colombian Peso (COP) string.
     * Example: 45000.0 -> "$45.000,00"
     */
    fun formatCop(amount: Double): String {
        synchronized(copFormat) {
            return copFormat.format(amount)
        }
    }
}

/**
 * Extension function to format any Double into a COP currency string.
 */
fun Double.toCopCurrency(): String = CurrencyFormatter.formatCop(this)
