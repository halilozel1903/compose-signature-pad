package io.github.halilozel1903.signaturepad.core

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Formats [value] with at most two decimals and no trailing zeros, independent of the locale:
 * `3f` gives `3`, `2.5f` gives `2.5`, `-0.004f` gives `0`.
 */
internal fun formatNumber(value: Float): String {
    val hundredths = (value.toDouble() * 100.0).roundToLong()
    if (hundredths == 0L) return "0"
    val sign = if (hundredths < 0) "-" else ""
    val magnitude = abs(hundredths)
    val whole = magnitude / 100
    val fraction = magnitude % 100
    return when {
        fraction == 0L -> "$sign$whole"
        fraction % 10 == 0L -> "$sign$whole.${fraction / 10}"
        fraction < 10 -> "$sign$whole.0$fraction"
        else -> "$sign$whole.$fraction"
    }
}

/** `#RRGGBB` of an ARGB color, alpha dropped. */
internal fun hexRgb(argb: Int): String {
    val rgb = argb and 0xFFFFFF
    return "#" + rgb.toString(16).uppercase().padStart(6, '0')
}

/** The alpha of an ARGB color as 0 to 1. */
internal fun alphaOf(argb: Int): Float = ((argb ushr 24) and 0xFF) / 255f
