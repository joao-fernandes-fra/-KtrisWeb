package webktris.view

fun String.withAlpha(a: Double): String {
    val hex = removePrefix("#")
    val r = hex.substring(0, 2).toInt(16)
    val g = hex.substring(2, 4).toInt(16)
    val b = hex.substring(4, 6).toInt(16)
    return "rgba($r,$g,$b,$a)"
}

fun lighten(hex: String, amount: Double): String {
    val (r, g, b) = hexToRgb(hex)
    return "rgb(${(r + 255 * amount).toInt().coerceIn(0, 255)}, ${
        (g + 255 * amount).toInt().coerceIn(0, 255)
    }, ${(b + 255 * amount).toInt().coerceIn(0, 255)})"
}

fun darken(hex: String, amount: Double): String {
    val (r, g, b) = hexToRgb(hex)
    return "rgb(${(r * (1 - amount)).toInt().coerceIn(0, 255)}, ${
        (g * (1 - amount)).toInt().coerceIn(0, 255)
    }, ${(b * (1 - amount)).toInt().coerceIn(0, 255)})"
}

fun hexToRgb(hex: String): Triple<Int, Int, Int> {
    val clean = hex.removePrefix("#")
    return Triple(
        clean.substring(0, 2).toInt(16),
        clean.substring(2, 4).toInt(16),
        clean.substring(4, 6).toInt(16)
    )
}