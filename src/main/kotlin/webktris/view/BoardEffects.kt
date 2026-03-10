package webktris.view

import engine.model.Piece
import engine.model.PieceState
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import webktris.model.RenderState
import kotlin.math.PI
import kotlin.random.Random

class BoardEffects(
    private val ctx: CanvasRenderingContext2D,
    private val state: RenderState,
    private val pieceRenderer: PieceRenderer
) {
    private val blockSize get() = pieceRenderer.blockSize

    companion object {
        private const val SPIN_DURATION = 600.0

        private val LINE_CLEAR_COLORS = mapOf(
            1 to "#00cfff",
            2 to "#00ff99",
            3 to "#ff9900",
            4 to "#ff00ff"
        )
    }

    fun drawLineClearEffects(offsetX: Double, offsetY: Double, boardWidth: Double, bufferSize: Int) {
        val now = window.performance.now()

        state.activeClearEffects.addAll(state.linesToClear.map {
            makeClearAnimation(it, boardWidth, offsetX, offsetY, bufferSize)
        })
        state.linesToClear.clear()
        state.activeClearEffects.removeAll { now - it.startTime > it.duration }

        state.activeClearEffects.forEach { effect ->
            val elapsed = now - effect.startTime
            val progress = (elapsed / effect.duration).coerceIn(0.0, 1.0)
            val alpha = (1.0 - progress) * (1.0 - progress)
            val baseColor = LINE_CLEAR_COLORS[effect.lineCount] ?: "#ffffff"

            ctx.save()

            effect.rows.forEach { row ->
                val visRow = row - bufferSize
                if (visRow < 0) return@forEach
                val rowY = offsetY + visRow * blockSize

                val barGrad = ctx.createLinearGradient(offsetX, rowY, offsetX + boardWidth, rowY)
                barGrad.addColorStop(0.0, "rgba(0,0,0,0)")
                barGrad.addColorStop(0.2, baseColor.withAlpha(alpha * 0.9))
                barGrad.addColorStop(0.5, "rgba(255,255,255,${alpha * 0.95})")
                barGrad.addColorStop(0.8, baseColor.withAlpha(alpha * 0.9))
                barGrad.addColorStop(1.0, "rgba(0,0,0,0)")
                ctx.fillStyle = barGrad
                ctx.fillRect(offsetX, rowY, boardWidth, blockSize)

                ctx.strokeStyle = "rgba(255,255,255,$alpha)"
                ctx.lineWidth = 2.0
                ctx.beginPath()
                ctx.moveTo(offsetX, rowY + blockSize / 2)
                ctx.lineTo(offsetX + boardWidth, rowY + blockSize / 2)
                ctx.stroke()
            }

            effect.particles.forEach { p ->
                val t = elapsed / 1000.0
                val px = p.x + p.vx * elapsed * 0.06
                val py = p.y + p.vy * elapsed * 0.06 + 0.5 * 200 * t * t
                val r = (blockSize * 0.12 * (1.0 - progress)).coerceAtLeast(1.0)
                ctx.fillStyle = baseColor.withAlpha(alpha)
                ctx.beginPath()
                ctx.arc(px, py, r, 0.0, PI * 2)
                ctx.fill()
            }

            ctx.restore()
        }
    }

    fun <T : Piece> animateSpin(ox: Double, oy: Double, bufferSize: Int, piece: PieceState<T>?) {
        piece ?: return
        val elapsed = window.performance.now() - state.spinStartTime
        if (elapsed > SPIN_DURATION) return

        val progress = (elapsed / SPIN_DURATION).coerceIn(0.0, 1.0)
        val alpha = 1.0 - progress
        val isFull = state.lastSpinType?.name == "FULL"

        val glowColor = if (isFull) "#ff00ff" else "#00f0f0"
        val r = if (isFull) 255 else 0
        val g = if (isFull) 0 else 240
        val b = if (isFull) 255 else 240

        ctx.save()
        ctx.globalAlpha = alpha

        val matrix = piece.shape
        val centerX = ox + (piece.col + matrix.cols / 2.0) * blockSize
        val centerY = oy + ((piece.row - bufferSize) + matrix.rows / 2.0) * blockSize
        val radius = (matrix.cols.coerceAtLeast(matrix.rows) / 2.0) * blockSize

        if (isFull) {
            val ringRadius = radius + progress * blockSize * 2.5
            ctx.shadowBlur = 20.0
            ctx.shadowColor = glowColor
            ctx.strokeStyle = "rgba($r,$g,$b,$alpha)"
            ctx.lineWidth = 3.0 * (1.0 - progress)
            ctx.beginPath()
            ctx.arc(centerX, centerY, ringRadius, 0.0, PI * 2)
            ctx.stroke()

            val rayLen = blockSize * progress
            ctx.lineWidth = 2.0 * (1.0 - progress)
            for (i in 0 until 8) {
                val angle = (i * PI / 4) + (progress * PI * 0.25)
                val x1 = centerX + kotlin.math.cos(angle) * ringRadius * 0.7
                val y1 = centerY + kotlin.math.sin(angle) * ringRadius * 0.7
                val x2 = centerX + kotlin.math.cos(angle) * (ringRadius * 0.7 + rayLen)
                val y2 = centerY + kotlin.math.sin(angle) * (ringRadius * 0.7 + rayLen)
                ctx.beginPath()
                ctx.moveTo(x1, y1)
                ctx.lineTo(x2, y2)
                ctx.stroke()
            }
        }

        ctx.shadowBlur = if (isFull) 18.0 * (1.0 - progress) else 10.0 * (1.0 - progress)
        ctx.shadowColor = glowColor
        ctx.strokeStyle = "rgba($r,$g,$b,$alpha)"
        ctx.lineWidth = (if (isFull) 3.5 else 2.0) * (1.0 - progress)
        if (!isFull) ctx.setLineDash(arrayOf(blockSize * 0.3, blockSize * 0.2))

        val expand = progress * blockSize * 0.35
        for (row in 0 until matrix.rows) {
            for (col in 0 until matrix.cols) {
                if (matrix[row, col] != 0) {
                    val visRow = (piece.row + row) - bufferSize
                    if (visRow < 0) continue
                    val px = ox + (piece.col + col) * blockSize
                    val py = oy + visRow * blockSize
                    ctx.strokeRect(px - expand, py - expand, blockSize + expand * 2, blockSize + expand * 2)
                }
            }
        }

        ctx.setLineDash(emptyArray())
        ctx.restore()
    }

    fun makeClearAnimation(
        rows: Collection<Int>,
        boardWidth: Double,
        offsetX: Double,
        offsetY: Double,
        bufferSize: Int,
        duration: Double = 500.0
    ): ClearAnimation {
        val particles = (0..60).map {
            ClearParticle(
                x = offsetX + Random.nextDouble() * boardWidth,
                y = offsetY + ((rows.random() - bufferSize) * blockSize) + blockSize / 2,
                vx = (Random.nextDouble() - 0.5) * 6.0,
                vy = (Random.nextDouble() - 1.1) * 4.0
            )
        }
        return ClearAnimation(rows, window.performance.now(), duration, rows.size, particles)
    }
}