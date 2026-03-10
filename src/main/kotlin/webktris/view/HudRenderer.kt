package webktris.view

import engine.model.GameGoal
import engine.model.KtrisContext
import engine.model.Piece
import kotlinx.browser.window
import org.w3c.dom.*
import webktris.model.RenderState

class HudRenderer(
    private val ctx: CanvasRenderingContext2D,
    private val pieceRenderer: PieceRenderer,
) {

    companion object {
        private const val FONT_FAMILY = "monospace"
        private const val LEVEL_FLASH_DURATION = 800.0
    }


    fun drawHeldPiece(
        offsetX: Double, offsetY: Double,
        piece: Piece?, hudFontSize: Double, previewBlockSize: Double
    ) {
        val boxWidth = 4 * previewBlockSize
        val boxHeight = 4 * previewBlockSize

        ctx.save()

        ctx.strokeStyle = "rgba(0,255,255,0.5)"
        ctx.lineWidth = 2.0
        ctx.fillStyle = "rgba(0,0,0,0.55)"
        ctx.fillRoundRect(offsetX, offsetY, boxWidth, boxHeight + hudFontSize + 10.0, 4.0)
        ctx.strokeRoundRect(offsetX, offsetY, boxWidth, boxHeight + hudFontSize + 10.0, 4.0)

        ctx.fillStyle = "rgba(255,255,255,0.6)"
        ctx.font = "${hudFontSize * 0.85}px $FONT_FAMILY"
        ctx.textAlign = CanvasTextAlign.CENTER
        ctx.fillText("HOLD", offsetX + boxWidth / 2, offsetY + hudFontSize)

        piece?.let {
            pieceRenderer.drawPieceMatrix(
                it.shape,
                offsetX,
                offsetY + hudFontSize + 6.0,
                previewBlockSize,
                it.id,
                centerInBox = true
            )
        }

        ctx.restore()
    }

    fun <T : Piece> drawNextPanel(
        x: Double, y: Double,
        nextPieces: List<T?>, hudFontSize: Double, previewBlockSize: Double
    ) {
        val boxWidth = 4 * previewBlockSize
        val slotHeight = 4 * previewBlockSize + 8.0
        val panelHeight = hudFontSize + 10.0 + slotHeight * nextPieces.size

        ctx.save()

        ctx.fillStyle = "rgba(0,0,0,0.55)"
        ctx.strokeStyle = "rgba(0,255,255,0.5)"
        ctx.lineWidth = 2.0
        ctx.fillRoundRect(x, y, boxWidth, panelHeight, 4.0)
        ctx.strokeRoundRect(x, y, boxWidth, panelHeight, 4.0)

        ctx.fillStyle = "rgba(255,255,255,0.6)"
        ctx.font = "${hudFontSize * 0.85}px $FONT_FAMILY"
        ctx.textAlign = CanvasTextAlign.CENTER
        ctx.fillText("NEXT", x + boxWidth / 2, y + hudFontSize)

        ctx.strokeStyle = "rgba(255,255,255,0.06)"
        ctx.lineWidth = 1.0

        nextPieces.filterNotNull().forEachIndexed { index, piece ->
            val slotY = y + hudFontSize + 10.0 + index * slotHeight

            if (index > 0) {
                ctx.beginPath()
                ctx.moveTo(x + 6.0, slotY)
                ctx.lineTo(x + boxWidth - 6.0, slotY)
                ctx.stroke()
            }

            val scale = when (index) {
                0 -> 1.0; 1 -> 0.9; else -> 0.8
            }
            val size = previewBlockSize * scale

            val boxX = x + (boxWidth - 4 * size) / 2
            val boxY = slotY + (slotHeight - 4 * size) / 2

            pieceRenderer.drawPieceMatrix(
                piece.shape,
                boxX, boxY,
                size,
                piece.id,
                centerInBox = true
            )
        }
        ctx.restore()
    }

    fun <T : Piece> drawHUD(
        offsetX: Double, offsetY: Double,
        boardWidth: Double, hudFontSize: Double,
        gameContext: KtrisContext<T>,
        totalLinesCleared: Int, totalPoints: Double
    ) {
        ctx.save()
        val hudPadding = 20.0

        ctx.textAlign = CanvasTextAlign.RIGHT
        ctx.fillStyle = "rgba(255,255,255,0.5)"
        ctx.font = "${hudFontSize * 1.2}px $FONT_FAMILY"
        ctx.fillText("LINES", offsetX - hudPadding, offsetY - 28.0)

        ctx.fillStyle = "rgba(255,255,255,0.9)"
        ctx.font = "bold ${hudFontSize * 0.8}px $FONT_FAMILY"
        val goalLines = gameContext.gameSettings.goalValue
            .takeIf { gameContext.gameSettings.goalType == GameGoal.LINES } ?: "∞"
        ctx.fillText("$totalLinesCleared/$goalLines", offsetX - hudPadding, offsetY - 8.0)

        ctx.textAlign = CanvasTextAlign.LEFT
        ctx.fillStyle = "rgba(255,255,255,0.5)"
        ctx.font = "${hudFontSize * 1.2}px $FONT_FAMILY"
        ctx.fillText("SCORE", offsetX + boardWidth + hudPadding, offsetY - 28.0)

        ctx.fillStyle = "rgba(255,255,255,0.9)"
        ctx.font = "bold ${hudFontSize * 0.8}px $FONT_FAMILY"
        ctx.fillText(totalPoints.toInt().toString(), offsetX + boardWidth + hudPadding, offsetY - 8.0)

        ctx.restore()
    }

    fun drawLevelDisplay(x: Double, y: Double, state: RenderState, hudFontSize: Double, previewBlockSize: Double) {
        val boxWidth = 4 * previewBlockSize
        val now = window.performance.now()

        ctx.save()

        ctx.fillStyle = "rgba(255,255,255,0.7)"
        ctx.font = "${hudFontSize}px $FONT_FAMILY"
        ctx.textAlign = CanvasTextAlign.CENTER
        ctx.fillText("LEVEL", x + boxWidth / 2, y + hudFontSize)

        ctx.fillStyle = "rgba(26,26,26,0.5)"
        ctx.fillRect(x, y + hudFontSize + 8.0, boxWidth, boxWidth * 0.6)

        val flashProgress = ((now - state.levelUpFlashTime) / LEVEL_FLASH_DURATION).coerceIn(0.0, 1.0)
        ctx.shadowBlur = (1.0 - flashProgress) * 20.0 + 4.0
        ctx.shadowColor = "#bd00ff"

        ctx.fillStyle = "white"
        ctx.font = "bold ${hudFontSize * 1.6}px $FONT_FAMILY"
        ctx.fillText(
            state.currentLevel.toString(),
            x + boxWidth / 2,
            y + hudFontSize + 8.0 + (boxWidth * 0.6 / 2) + (hudFontSize * 0.55)
        )

        ctx.restore()
    }

    fun drawNotifications(
        offsetX: Double,
        offsetY: Double,
        boardWidth: Double,
        boardHeight: Double,
        state: RenderState,
        notificationFontSize: Double,
    ) {
        val now = window.performance.now()
        state.messageQueue.removeAll { now - it.timestamp > 1500 }
        if (state.messageQueue.isEmpty()) return

        val notifyX = offsetX + boardWidth / 2.0
        val notifyStartY = offsetY + boardHeight * 0.35

        ctx.save()
        ctx.textAlign = CanvasTextAlign.CENTER

        state.messageQueue.filter { it.text.isNotEmpty() }.forEachIndexed { i, msg ->
            val elapsed = now - msg.timestamp
            val progress = elapsed / 1500.0
            val alpha = (1.0 - progress) * (1.0 - progress)
            val moveUp = progress * 40.0

            val y = notifyStartY + (i * (notificationFontSize + 12.0)) - moveUp

            val metrics = ctx.measureText(msg.text.uppercase())
            val pillPadX = notificationFontSize * 0.5
            val pillPadY = notificationFontSize * 0.3
            ctx.fillStyle = "rgba(0,0,0,${alpha * 0.45})"
            ctx.beginPath()
            ctx.fillRoundRect(
                notifyX - metrics.width / 2 - pillPadX,
                y - notificationFontSize - pillPadY,
                metrics.width + pillPadX * 2,
                notificationFontSize + pillPadY * 2,
                notificationFontSize * 0.25
            )
            ctx.fill()

            ctx.font = "italic bold ${notificationFontSize}px $FONT_FAMILY"
            ctx.shadowBlur = 10.0
            ctx.shadowColor = "rgba(255,255,255,${alpha * 0.6})"
            ctx.fillStyle = "rgba(255,255,255,$alpha)"
            ctx.fillText(msg.text.uppercase(), notifyX, y)
        }
        ctx.restore()
    }

    fun drawBorder(
        offsetX: Double, offsetY: Double,
        boardWidth: Double, boardHeight: Double,
        bufferSize: Int, blockSize: Double
    ) {
        val bufferHeight = bufferSize * blockSize
        ctx.save()

        ctx.fillStyle = "rgba(0,0,0,0.55)"
        ctx.fillRect(offsetX, offsetY, boardWidth, boardHeight)

        ctx.save()
        ctx.setLineDash(arrayOf(4.0, 6.0))
        ctx.lineWidth = 2.0
        listOf(offsetX, offsetX + boardWidth).forEach { bx ->
            val grad = ctx.createLinearGradient(bx, offsetY - bufferHeight, bx, offsetY)
            grad.addColorStop(0.0, "rgba(0,255,255,0.0)")
            grad.addColorStop(1.0, "rgba(0,255,255,0.35)")
            ctx.strokeStyle = grad
            ctx.beginPath()
            ctx.moveTo(bx, offsetY - bufferHeight)
            ctx.lineTo(bx, offsetY)
            ctx.stroke()
        }
        ctx.restore()

        val railGlow = ctx.createLinearGradient(offsetX, offsetY, offsetX, offsetY + boardHeight)
        railGlow.addColorStop(0.0, "#00ffff")
        railGlow.addColorStop(0.5, "#bd00ff")
        railGlow.addColorStop(1.0, "#00ffff")
        ctx.strokeStyle = railGlow
        ctx.shadowBlur = 15.0
        ctx.shadowColor = "#00ffff"
        ctx.lineWidth = 3.0

        ctx.beginPath()
        ctx.moveTo(offsetX, offsetY)
        ctx.lineTo(offsetX, offsetY + boardHeight)
        ctx.lineTo(offsetX + boardWidth, offsetY + boardHeight)
        ctx.lineTo(offsetX + boardWidth, offsetY)
        ctx.stroke()

        ctx.shadowColor = "#bd00ff"
        ctx.shadowBlur = 20.0
        ctx.beginPath()
        ctx.moveTo(offsetX, offsetY + boardHeight)
        ctx.lineTo(offsetX + boardWidth, offsetY + boardHeight)
        ctx.stroke()

        ctx.restore()
    }

    fun drawFinishScreen(canvasWidth: Int, canvasHeight: Int, finishMessage: String?, finishFontSize: Double) {
        ctx.fillStyle = "rgba(0,0,0,0.7)"
        ctx.fillRect(0.0, 0.0, canvasWidth.toDouble(), canvasHeight.toDouble())
        ctx.fillStyle = "white"
        ctx.font = "bold ${finishFontSize}px $FONT_FAMILY"
        ctx.textAlign = CanvasTextAlign.CENTER
        ctx.fillText(finishMessage ?: "", canvasWidth / 2.0, canvasHeight / 2.0)
        ctx.textAlign = CanvasTextAlign.START
    }

    fun drawFreezeGauge(x: Double, y: Double, state: RenderState, hudFontSize: Double, previewBlockSize: Double) {
        val boxWidth = 4 * previewBlockSize
        val gaugeHeight = previewBlockSize * 5.5
        val now = window.performance.now()
        val isFull = state.gaugeValue >= 40.0
        val fillRatio = (state.gaugeValue / 40.0).coerceIn(0.0, 1.0)
        val isActive = state.isZoneActive

        val flashProgress = ((now - state.gaugeFullFlashTime) / 800.0).coerceIn(0.0, 1.0)

        ctx.save()
        ctx.textAlign = CanvasTextAlign.CENTER

        val label = when {
            isActive -> "ZONE ACTIVE"
            isFull -> "ZONE READY"
            else -> "ZONE"
        }
        val labelColor = when {
            isActive -> "rgba(0, 220, 255, 0.95)"
            isFull -> "rgba(255, 255, 255, 0.95)"
            else -> "rgba(255, 255, 255, 0.7)"
        }
        if (isActive) {
            ctx.shadowBlur = 10.0
            ctx.shadowColor = "#00dcff"
        }
        ctx.fillStyle = labelColor
        ctx.font = "bold ${hudFontSize}px $FONT_FAMILY"
        ctx.fillText(label, x + boxWidth / 2, y + hudFontSize)
        ctx.shadowBlur = 0.0

        val barTop = y + hudFontSize + 8.0
        val barLeft = x + boxWidth * 0.25
        val barWidth = boxWidth * 0.5

        ctx.fillStyle = "rgba(26,26,26,0.6)"
        ctx.fillRoundRect(barLeft, barTop, barWidth, gaugeHeight, barWidth / 2)

        if (fillRatio > 0.0) {
            val fillHeight = gaugeHeight * fillRatio
            val fillY = barTop + gaugeHeight - fillHeight

            val fillGrad = ctx.createLinearGradient(barLeft, fillY + fillHeight, barLeft, fillY)
            when {
                isActive -> {
                    fillGrad.addColorStop(0.0, "#0077ff")
                    fillGrad.addColorStop(0.5, "#00dcff")
                    fillGrad.addColorStop(1.0, "#ffffff")
                }

                isFull -> {
                    fillGrad.addColorStop(0.0, "#ff00ff")
                    fillGrad.addColorStop(0.5, "#bd00ff")
                    fillGrad.addColorStop(1.0, "#ffffff")
                }

                else -> {
                    fillGrad.addColorStop(0.0, "#00cfff")
                    fillGrad.addColorStop(1.0, "#bd00ff")
                }
            }

            ctx.save()
            ctx.beginPath()
            ctx.fillRoundRectPath(barLeft, barTop, barWidth, gaugeHeight, barWidth / 2)
            ctx.clip()
            ctx.fillStyle = fillGrad
            ctx.fillRect(barLeft, fillY, barWidth, fillHeight)

            if (isActive) {
                val sweepSpeed = 1200.0
                val sweepPos = ((now % sweepSpeed) / sweepSpeed)
                val sweepY = fillY + (fillHeight * sweepPos)
                val sweepHeight = fillHeight * 0.18

                val sweepGrad = ctx.createLinearGradient(barLeft, sweepY, barLeft, sweepY + sweepHeight)
                sweepGrad.addColorStop(0.0, "rgba(255,255,255,0.0)")
                sweepGrad.addColorStop(0.4, "rgba(255,255,255,0.55)")
                sweepGrad.addColorStop(1.0, "rgba(255,255,255,0.0)")
                ctx.fillStyle = sweepGrad
                ctx.fillRect(barLeft, sweepY, barWidth, sweepHeight)
            }

            ctx.restore()
        }

        val (glowColor, pulseAlpha) = when {
            isActive -> {
                val pulse = 0.5 + (kotlin.math.sin(now / 150.0) * 0.3).coerceIn(-0.3, 0.3)
                "#00dcff" to pulse
            }

            isFull -> {
                val pulse = 0.4 + (kotlin.math.sin(now / 200.0) * 0.3).coerceIn(-0.4, 0.4)
                "#ff00ff" to pulse
            }

            else -> "#000000" to 0.0
        }
        if (pulseAlpha > 0.0) {
            ctx.shadowBlur = if (isActive) 25.0 else 20.0
            ctx.shadowColor = glowColor
            ctx.strokeStyle = glowColor.withAlpha(pulseAlpha)
            ctx.lineWidth = if (isActive) 3.0 else 2.0
            ctx.strokeRoundRect(barLeft, barTop, barWidth, gaugeHeight, barWidth / 2)
            ctx.shadowBlur = 0.0
        }

        ctx.strokeStyle = "rgba(0,0,0,0.4)"
        ctx.lineWidth = 1.0
        for (tick in 1..3) {
            val tickY = barTop + gaugeHeight * (1.0 - tick / 4.0)
            ctx.beginPath()
            ctx.moveTo(barLeft, tickY)
            ctx.lineTo(barLeft + barWidth, tickY)
            ctx.stroke()
        }

        val bottomLabelY = barTop + gaugeHeight + hudFontSize + 6.0
        when {
            isActive -> {
                ctx.shadowBlur = 8.0
                ctx.shadowColor = "#00dcff"
                ctx.fillStyle = "#00dcff"
                ctx.font = "bold ${hudFontSize * 1.2}px $FONT_FAMILY"
                ctx.fillText("${state.gaugeValue.toInt()}", x + boxWidth / 2, bottomLabelY)
            }

            isFull -> {
                ctx.shadowBlur = 14.0 * (1.0 - flashProgress) + 6.0
                ctx.shadowColor = "#ff00ff"
                ctx.fillStyle = "white"
                ctx.font = "bold ${hudFontSize * 1.2}px $FONT_FAMILY"
                ctx.fillText("MAX", x + boxWidth / 2, bottomLabelY)
            }

            else -> {
                ctx.shadowBlur = 0.0
                ctx.fillStyle = "rgba(255,255,255,0.6)"
                ctx.font = "${hudFontSize * 0.9}px $FONT_FAMILY"
                ctx.fillText("${state.gaugeValue.toInt()}/40", x + boxWidth / 2, bottomLabelY)
            }
        }

        ctx.restore()
    }

    fun CanvasRenderingContext2D.fillRoundRect(x: Double, y: Double, w: Double, h: Double, r: Double) {
        beginPath()
        moveTo(x + r, y)
        lineTo(x + w - r, y)
        arcTo(x + w, y, x + w, y + r, r)
        lineTo(x + w, y + h - r)
        arcTo(x + w, y + h, x + w - r, y + h, r)
        lineTo(x + r, y + h)
        arcTo(x, y + h, x, y + h - r, r)
        lineTo(x, y + r)
        arcTo(x, y, x + r, y, r)
        closePath()
        fill()
    }

    fun CanvasRenderingContext2D.fillRoundRectPath(x: Double, y: Double, w: Double, h: Double, r: Double) {
        beginPath()
        moveTo(x + r, y)
        lineTo(x + w - r, y)
        arcTo(x + w, y, x + w, y + r, r)
        lineTo(x + w, y + h - r)
        arcTo(x + w, y + h, x + w - r, y + h, r)
        lineTo(x + r, y + h)
        arcTo(x, y + h, x, y + h - r, r)
        lineTo(x, y + r)
        arcTo(x, y, x + r, y, r)
        closePath()
    }

    fun CanvasRenderingContext2D.strokeRoundRect(x: Double, y: Double, w: Double, h: Double, r: Double) {
        fillRoundRectPath(x, y, w, h, r)
        stroke()
    }
}