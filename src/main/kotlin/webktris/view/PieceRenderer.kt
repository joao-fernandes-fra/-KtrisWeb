package webktris.view

import engine.model.Matrix
import engine.model.Piece
import engine.model.PieceState
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import kotlin.math.sin

class PieceRenderer(
    private val ctx: CanvasRenderingContext2D,
    private val colors: Map<Int, String>
) {
    var blockSize = 30.0

    fun drawBlock(r: Int, c: Int, color: String, offsetX: Double, offsetY: Double, alpha: Double = 1.0) {
        val x = offsetX + (c * blockSize) + 1
        val y = offsetY + (r * blockSize) + 1
        val size = blockSize - 2

        ctx.save()
        ctx.globalAlpha = alpha

        val baseGrad = ctx.createLinearGradient(x, y, x, y + size)
        baseGrad.addColorStop(0.0, lighten(color, 0.25))
        baseGrad.addColorStop(0.5, color)
        baseGrad.addColorStop(1.0, darken(color, 0.35))
        ctx.fillStyle = baseGrad
        ctx.fillRect(x, y, size, size)

        ctx.strokeStyle = lighten(color, 0.55)
        ctx.lineWidth = (blockSize * 0.07).coerceAtLeast(1.0)
        ctx.beginPath()
        ctx.moveTo(x + size, y)
        ctx.lineTo(x, y)
        ctx.lineTo(x, y + size)
        ctx.stroke()

        ctx.strokeStyle = darken(color, 0.5)
        ctx.beginPath()
        ctx.moveTo(x + size, y)
        ctx.lineTo(x + size, y + size)
        ctx.lineTo(x, y + size)
        ctx.stroke()

        val glossSize = size * 0.35
        val glossGrad = ctx.createRadialGradient(
            x + glossSize * 0.5, y + glossSize * 0.5, 0.0,
            x + glossSize * 0.5, y + glossSize * 0.5, glossSize
        )
        glossGrad.addColorStop(0.0, "rgba(255,255,255,0.30)")
        glossGrad.addColorStop(1.0, "rgba(255,255,255,0.00)")
        ctx.fillStyle = glossGrad
        ctx.fillRect(x, y, glossSize, glossSize)

        ctx.restore()
    }

    fun drawPendingBlock(r: Int, c: Int, offsetX: Double, offsetY: Double) {
        val pulse = (sin(window.performance.now() / 150.0) * 0.25 + 0.75)
        ctx.fillStyle = "rgba(255, 255, 255, $pulse)"
        ctx.fillRect(offsetX + (c * blockSize), offsetY + (r * blockSize), blockSize, blockSize)
    }

    fun drawPieceMatrix(
        shape: Matrix,
        x: Double,
        y: Double,
        size: Double,
        colorId: Int,
        centerInBox: Boolean = false
    ) {
        var drawX = x
        var drawY = y

        if (centerInBox) {
            drawX += (4 * size - shape.cols * size) / 2
            drawY += (4 * size - shape.rows * size) / 2

            if (shape.rows == 1) drawY += size * 0.5
        }

        val originalSize = blockSize
        blockSize = size
        for (r in 0 until shape.rows) {
            for (c in 0 until shape.cols) {
                if (shape[r, c] != 0) {
                    drawBlock(0, 0, colors[colorId] ?: "white", drawX + (c * size), drawY + (r * size))
                }
            }
        }
        blockSize = originalSize
    }

    fun <T : Piece> drawPieceState(piece: PieceState<T>, offsetX: Double, offsetY: Double, buffer: Int, alpha: Double) {
        val matrix = piece.shape
        for (r in 0 until matrix.rows) {
            for (c in 0 until matrix.cols) {
                if (matrix[r, c] != 0) {
                    val row = (piece.row + r) - buffer
                    drawBlock(row, piece.col + c, colors[piece.type.id] ?: "white", offsetX, offsetY, alpha)
                }
            }
        }
    }
}