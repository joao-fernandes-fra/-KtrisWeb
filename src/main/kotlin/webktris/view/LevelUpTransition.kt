package webktris.view

import kotlinx.browser.window
import org.w3c.dom.CENTER
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.CanvasTextAlign
import kotlin.math.PI

class LevelUpTransition(private val ctx: CanvasRenderingContext2D) {

    companion object {
        private const val FLASH_DURATION = 300.0
        private const val SHOCKWAVE_DURATION = 700.0
        private const val BANNER_DURATION = 2200.0
        private const val FONT_FAMILY = "monospace"
    }

    private data class LevelUpEvent(
        val level: Int,
        val startTime: Double
    )

    private val queue = mutableListOf<LevelUpEvent>()

    fun trigger(level: Int) {
        queue.add(LevelUpEvent(level, window.performance.now()))
    }

    fun draw(
        canvasWidth: Int,
        canvasHeight: Int,
        offsetX: Double,
        offsetY: Double,
        boardWidth: Double,
        boardHeight: Double,
        hudFontSize: Double
    ) {
        if (queue.isEmpty()) return
        val now = window.performance.now()
        val w = canvasWidth.toDouble()
        val h = canvasHeight.toDouble()
        val centerX = offsetX + boardWidth / 2
        val centerY = offsetY + boardHeight / 2

        queue.removeAll { now - it.startTime > BANNER_DURATION }

        queue.forEach { event ->
            val elapsed = now - event.startTime

            if (elapsed < FLASH_DURATION) {
                val flashProgress = elapsed / FLASH_DURATION
                val alpha = (1.0 - flashProgress) * (1.0 - flashProgress) * 0.45
                ctx.save()
                ctx.fillStyle = "rgba(255,255,255,$alpha)"
                ctx.fillRect(0.0, 0.0, w, h)
                ctx.restore()
            }

            if (elapsed < SHOCKWAVE_DURATION) {
                val waveProgress = elapsed / SHOCKWAVE_DURATION
                val eased = 1.0 - (1.0 - waveProgress) * (1.0 - waveProgress)
                val maxRadius = (boardWidth.coerceAtLeast(boardHeight)) * 0.85
                val radius = maxRadius * eased
                val alpha = (1.0 - waveProgress) * 0.9

                ctx.save()
                ctx.beginPath()
                ctx.arc(centerX, centerY, radius, 0.0, PI * 2)
                ctx.strokeStyle = "rgba(255,255,255,$alpha)"
                ctx.lineWidth = 4.0 * (1.0 - waveProgress)
                ctx.shadowBlur = 20.0 * (1.0 - waveProgress)
                ctx.shadowColor = "#ffffff"
                ctx.stroke()

                val innerRadius = maxRadius * (eased * 0.75)
                val innerAlpha = alpha * 0.5
                ctx.beginPath()
                ctx.arc(centerX, centerY, innerRadius, 0.0, PI * 2)
                ctx.strokeStyle = "rgba(180,0,255,$innerAlpha)"
                ctx.lineWidth = 2.0 * (1.0 - waveProgress)
                ctx.shadowColor = "#bd00ff"
                ctx.stroke()
                ctx.restore()
            }

            if (elapsed < BANNER_DURATION) {
                val holdStart = 400.0
                val holdEnd = 1600.0
                val slideInEnd = holdStart

                val bannerAlpha: Double
                val bannerOffsetY: Double

                when {
                    elapsed < slideInEnd -> {
                        val p = elapsed / slideInEnd
                        val eased = 1.0 - (1.0 - p) * (1.0 - p)
                        bannerAlpha = eased
                        bannerOffsetY = -40.0 * (1.0 - eased)
                    }

                    elapsed < holdEnd -> {
                        bannerAlpha = 1.0
                        bannerOffsetY = 0.0
                    }

                    else -> {
                        val p = (elapsed - holdEnd) / (BANNER_DURATION - holdEnd)
                        bannerAlpha = 1.0 - p
                        bannerOffsetY = 0.0
                    }
                }

                val bannerY = offsetY + boardHeight * 0.18 + bannerOffsetY
                val sublabelSize = hudFontSize * 0.75
                val levelSize = hudFontSize * 2.2

                ctx.save()
                ctx.textAlign = CanvasTextAlign.CENTER
                ctx.globalAlpha = bannerAlpha

                ctx.font = "${sublabelSize}px $FONT_FAMILY"
                ctx.fillStyle = "rgba(255,255,255,0.75)"
                ctx.shadowBlur = 0.0
                ctx.fillText("LEVEL", centerX, bannerY)

                ctx.font = "bold ${levelSize}px $FONT_FAMILY"
                ctx.shadowBlur = 30.0 * bannerAlpha
                ctx.shadowColor = "#bd00ff"
                ctx.fillStyle = "white"
                ctx.fillText(event.level.toString(), centerX, bannerY + levelSize * 0.9)

                val numWidth = levelSize * (event.level.toString().length) * 0.65
                val divY = bannerY + levelSize * 0.3
                val divGap = numWidth * 0.6 + 10.0
                val divLen = boardWidth * 0.18

                ctx.strokeStyle = "rgba(255,255,255,${0.4 * bannerAlpha})"
                ctx.lineWidth = 1.0
                ctx.shadowBlur = 0.0

                ctx.beginPath()
                ctx.moveTo(centerX - divGap - divLen, divY)
                ctx.lineTo(centerX - divGap, divY)
                ctx.stroke()

                ctx.beginPath()
                ctx.moveTo(centerX + divGap, divY)
                ctx.lineTo(centerX + divGap + divLen, divY)
                ctx.stroke()

                ctx.restore()
            }
        }
    }
}