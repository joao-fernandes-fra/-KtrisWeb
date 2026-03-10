package webktris.view

import engine.controller.GameRenderer
import engine.controller.defaults.ScoreProvider
import engine.model.Board
import engine.model.GameSnapshot
import engine.model.KtrisContext
import engine.model.Piece
import engine.model.defaults.Tetromino
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import webktris.model.RenderState
import kotlin.math.min

class CanvasRendering<T : Piece>(private val gameContext: KtrisContext<T>) : GameRenderer<T> {

    private val canvas = document.getElementById("canvas") as HTMLCanvasElement
    private val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
    private val gameId = gameContext.gameId
    private val scoreTracker = ScoreProvider.getTracker(gameId)

    private val colors = mapOf(
        Board.EMPTY_BLOCK_VALUE to "#1a1a1a",
        Tetromino.I.id to "#00ffff",
        Tetromino.J.id to "#0000ff",
        Tetromino.L.id to "#ff8c00",
        Tetromino.O.id to "#ffff00",
        Tetromino.S.id to "#00ff00",
        Tetromino.T.id to "#ff00ff",
        Tetromino.Z.id to "#ff0000",
        -1 to "#d3d3d3"
    )

    private val state = RenderState()
    private val pieceRenderer = PieceRenderer(ctx, colors)
    private val effects = BoardEffects(ctx, state, pieceRenderer)
    private val hud = HudRenderer(ctx, pieceRenderer)
    private val background = BackgroundRenderer(ctx)
    private val levelUpTransition = LevelUpTransition(ctx)

    private var previewBlockSize = 0.0
    private var hudFontSize = 0.0
    private var notificationFontSize = 0.0
    private var finishFontSize = 0.0

    companion object {
        private const val SIDE_PANEL_PADDING = 40.0
    }

    init {
        resizeCanvas()
        window.onresize = { resizeCanvas() }
        GameEventSubscriber(gameId, gameContext, state, background).subscribe()
    }

    private fun resizeCanvas() {
        canvas.width = window.innerWidth
        canvas.height = window.innerHeight
    }

    override fun render(snapshot: GameSnapshot<T>?) {
        if (snapshot == null) return
        updateSizes(snapshot)

        background.draw(canvas.width, canvas.height, state)

        state.pendingLevelUpLevel?.let {
            levelUpTransition.trigger(it)
            state.pendingLevelUpLevel = null
        }


        val boardWidth = snapshot.board.cols * pieceRenderer.blockSize
        val boardHeight = snapshot.board.visibleRows * pieceRenderer.blockSize
        val bufferHeight = snapshot.board.bufferSize * pieceRenderer.blockSize

        val totalHeight = boardHeight + bufferHeight
        val offsetX = (canvas.width - boardWidth) / 2.0
        val offsetY = (canvas.height - totalHeight) / 2.0 + bufferHeight

        effects.drawLineClearEffects(offsetX, offsetY, boardWidth, snapshot.board.bufferSize)
        hud.drawBorder(offsetX, offsetY, boardWidth, boardHeight, snapshot.board.bufferSize, pieceRenderer.blockSize)
        effects.animateSpin(offsetX, offsetY, snapshot.board.bufferSize, snapshot.currentPiece)
        drawBoard(snapshot, offsetX, offsetY)
        drawGhost(snapshot, offsetX, offsetY)
        drawCurrentPiece(snapshot, offsetX, offsetY)

        val holdX = offsetX - (4 * previewBlockSize) - SIDE_PANEL_PADDING
        hud.drawHeldPiece(holdX, offsetY, snapshot.holdPiece, hudFontSize, previewBlockSize)
        hud.drawLevelDisplay(
            holdX,
            offsetY + (4 * previewBlockSize) + SIDE_PANEL_PADDING,
            state,
            hudFontSize,
            previewBlockSize
        )
        hud.drawFreezeGauge(
            holdX,
            offsetY + (4 * previewBlockSize) + SIDE_PANEL_PADDING * 2 + hudFontSize * 3,
            state,
            hudFontSize,
            previewBlockSize
        )
        hud.drawNotifications(offsetX, offsetY, boardWidth, boardHeight, state, notificationFontSize)

        val nextX = offsetX + boardWidth + SIDE_PANEL_PADDING
        hud.drawNextPanel(nextX, offsetY, snapshot.nextPieces, hudFontSize, previewBlockSize)

        hud.drawHUD(
            offsetX, offsetY + boardHeight + 30.0, boardWidth,
            hudFontSize, gameContext,
            scoreTracker.totalLinesCleared, scoreTracker.totalPoints
        )

        levelUpTransition.draw(
            canvas.width, canvas.height,
            offsetX, offsetY, boardWidth, boardHeight,
            hudFontSize
        )

        if (state.gameFinished) hud.drawFinishScreen(canvas.width, canvas.height, state.finishMessage, finishFontSize)
    }

    private fun drawBoard(snapshot: GameSnapshot<T>, offsetX: Double, offsetY: Double) {
        val board = snapshot.board
        for (r in 0 until board.rows) {
            for (c in 0 until board.cols) {
                when (val id = board[r, c]) {
                    -1 -> pieceRenderer.drawPendingBlock(r - board.bufferSize, c, offsetX, offsetY)
                    0 -> Unit
                    else -> pieceRenderer.drawBlock(r - board.bufferSize, c, colors[id] ?: "white", offsetX, offsetY)
                }
            }
        }
    }

    private fun drawCurrentPiece(snapshot: GameSnapshot<T>, offsetX: Double, offsetY: Double) {
        snapshot.currentPiece?.let {
            pieceRenderer.drawPieceState(
                it,
                offsetX,
                offsetY,
                snapshot.board.bufferSize,
                1.0
            )
        }
    }

    private fun drawGhost(snapshot: GameSnapshot<T>, offsetX: Double, offsetY: Double) {
        snapshot.ghostPiece?.let { pieceRenderer.drawPieceState(it, offsetX, offsetY, snapshot.board.bufferSize, 0.3) }
    }

    private fun updateSizes(snapshot: GameSnapshot<T>) {
        val padding = 12
        val maxWidth = canvas.width / (snapshot.board.cols + padding)
        val maxHeight = canvas.height / (snapshot.board.visibleRows + snapshot.board.bufferSize + 2)
        pieceRenderer.blockSize = min(maxWidth, maxHeight).toDouble()
        previewBlockSize = pieceRenderer.blockSize * 0.7
        hudFontSize = pieceRenderer.blockSize * 0.55
        notificationFontSize = pieceRenderer.blockSize * 0.72
        finishFontSize = pieceRenderer.blockSize * 1.6
    }
}