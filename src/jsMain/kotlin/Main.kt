import engine.controller.defaults.GameRegistry
import engine.controller.defaults.ScoreProvider
import engine.model.GameGoal
import engine.model.GameSettings
import engine.model.PlayerSettings
import engine.model.defaults.Tetromino
import kotlinx.browser.window
import webktris.controller.WebTetris
import webktris.model.ExtendedRuleBook
import webktris.view.CanvasRendering
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun main() {
    window.onload = {
        js("window.ktrisRestart = function() { window.ktrisLaunch(); }")
        launchGame()
    }
}

@OptIn(ExperimentalUuidApi::class)
private fun launchGame() {
    val relaunch: () -> Unit = ::launchGame
    js("window.ktrisLaunch = relaunch")
    val cfg: dynamic = js("window.ktrisConfig")

    val gameSettings = GameSettings(
        boardRows = (cfg.boardRows as Int),
        boardCols = (cfg.boardCols as Int),
        bufferZone = (cfg.bufferZone as Int),
        gravityBase = (cfg.gravityBase as Double),
        gravityIncrement = (cfg.gravityIncrement as Double),
        levelCap = (cfg.levelCap as Int),
        shouldCollapseOnFreeze = (cfg.shouldCollapseOnFreeze as Boolean),
        goalType = when (cfg.goalType as String) {
            "LINES" -> GameGoal.LINES
            "TIME" -> GameGoal.TIME
            else -> GameGoal.NONE
        },
        goalValue = (cfg.goalValue as Double),
    )

    val playerSettings = PlayerSettings(
        dasDelay = (cfg.dasDelay as Double),
        arrDelay = (cfg.arrDelay as Double),
        entryDelay = (cfg.entryDelay as Double),
        lockDelay = (cfg.lockDelay as Double),
        softDropDelay = (cfg.softDropDelay as Double),
        maxLockResets = (cfg.maxLockResets as Int),
        isHoldEnabled = (cfg.isHoldEnabled as Boolean),
        isGhostEnabled = (cfg.isGhostEnabled as Boolean),
        isSpinEnabled = (cfg.isSpinEnabled as Boolean),
        is180Enabled = (cfg.is180Enabled as Boolean),
        previewSize = (cfg.previewSize as Int),
    )

    val gameId = Uuid.random().toString()
    val gameContext = GameRegistry.registerContext(
        GameRegistry.getDefaultContext(gameSettings, playerSettings, Tetromino.pieces, gameId)
    )
    ScoreProvider.defaultBuilder(gameId).withRuleBook(ExtendedRuleBook()).build()
    WebTetris(gameContext).start(CanvasRendering(gameContext))
}