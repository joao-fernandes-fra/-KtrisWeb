package webktris.controller

import engine.controller.GameRenderer
import engine.controller.defaults.DefaultTetrisEngine
import engine.model.KtrisContext
import engine.model.SpinType.NONE
import engine.model.TimeState
import engine.model.defaults.Logger
import engine.model.defaults.ProceduralPiece
import engine.model.events.EventOrchestrator
import engine.model.events.GameEvent
import engine.model.events.GameEvent.GarbageReceived
import engine.model.events.GameEvent.LevelUp
import engine.model.events.GameEvent.LineCleared
import engine.model.events.InputEvent
import engine.model.events.InputEvent.DirectionMoveEnd
import engine.model.events.InputEvent.DirectionMoveStart
import engine.model.events.InputEvent.DropInput
import engine.model.events.InputEvent.FreezeTime
import engine.model.events.InputEvent.RotationInputRelease
import engine.model.events.InputEvent.RotationInputStart
import engine.model.events.InputEvent.SlowDownTime
import kotlinx.browser.window

class WebTetris(
    private val context: KtrisContext<ProceduralPiece>
) : DefaultTetrisEngine<ProceduralPiece>(
    context.playerSettings,
    context.gameSettings,
    context.boardManager,
    context.pieceController,
    context.gameTimers,
    context.timeManager,
) {
    init {
        setupTimeSystem()
        setupInputEvents()
        setupGameEvents()
    }

    companion object {
        private const val MAX_GAUGE = 40.0
        private const val PASSIVE_DRAIN_PER_MS = 0.0008
    }

    private var zoneDrainPerMs = 0.0
    private var isZoneActive = false
    private var freezeGauge = 0.0
        set(v) {
            val wasAlreadyFull = field >= MAX_GAUGE
            field = v.coerceIn(0.0, MAX_GAUGE)
            if (field >= MAX_GAUGE && !wasAlreadyFull) {
                EventOrchestrator.publish(GaugeFull(gameId))
            }
            EventOrchestrator.publish(GaugeChanged(gameId, field))
        }

    private fun setupTimeSystem() {
        timeManager.onFreezeEnded = {
            freezeLineClears = 0
            val linesCleared = boardManager.clearFullLines()

            if (linesCleared.isNotEmpty()) {
                EventOrchestrator.publish(
                    LineCleared(NONE, linesCleared, boardManager.isBoardEmpty, gameId)
                )
            }
            Logger.info { "Freeze ended. Cleared $linesCleared lines immediately." }
        }
    }

    private fun setupGameEvents() {
        EventOrchestrator.subscribeForGameId<LevelUp>(gameId) { levelUp(it.newLevel) }
        EventOrchestrator.subscribeForGameId<GarbageReceived>(gameId) {
            processGarbage(it.lines)
        }
    }

    private fun setupInputEvents() {
        EventOrchestrator.subscribeForGameId<InputEvent.HoldInput>(gameId) { onHold() }
        EventOrchestrator.subscribeForGameId<DirectionMoveStart>(gameId) { onMovement(it.movement) }
        EventOrchestrator.subscribeForGameId<DirectionMoveEnd>(gameId) { onMovementRelease(it.movement) }
        EventOrchestrator.subscribeForGameId<DropInput>(gameId) { onDrop(it.dropType) }
        EventOrchestrator.subscribeForGameId<RotationInputStart>(gameId) { onRotation(it.rotation) }
        EventOrchestrator.subscribeForGameId<RotationInputRelease>(gameId) { onRotationRelease(it.rotation) }
        EventOrchestrator.subscribeForGameId<SlowDownTime>(gameId) { onTimeState(TimeState.SLOWED, it.duration) }
        EventOrchestrator.subscribeForGameId<FreezeTime>(gameId) { onTimeState(TimeState.FROZEN, it.duration) }
        EventOrchestrator.subscribeForGameId<InputEvent.ResetInput>(gameId) { reset() }
        EventOrchestrator.subscribeForGameId<LineCleared>(gameId) { event ->
            rechargeFreezeGauge(event)
        }

        EventOrchestrator.subscribeForGameId<GameEvent.SpinDetected>(gameId) { event ->
            if (event.spinType != NONE) freezeGauge += 1.5
        }

        EventOrchestrator.subscribeForGameId<ZoneInput>(gameId) {
            if (freezeGauge > 0.0) {
                activateZone()
            }
        }
    }

    private fun rechargeFreezeGauge(event: LineCleared) {
        val baseCharge = when (event.linesCleared.size) {
            1 -> 2.5
            2 -> 5.5
            3 -> 9.5
            4 -> 18.0
            else -> 0.0
        }
        val spinBonus = if (event.spinType != NONE) 1.5 else 1.0
        val allClearBonus = if (event.isEmptyBoard) 2.0 else 1.0

        freezeGauge += baseCharge * spinBonus * allClearBonus
    }

    private var isRunning: Boolean = false

    override val gameId: String get() = context.gameId

    override fun start(renderer: GameRenderer<ProceduralPiece>) {
        if (isRunning) return
        isRunning = true
        var lastTime = window.performance.now()

        BrowserInputHandler(context.gameId)
        fun loop(time: Double) {
            if (!isRunning) return

            val deltaTimeMs = time - lastTime
            lastTime = time
            val cappedDelta = if (deltaTimeMs > 100.0) 16.6 else deltaTimeMs

            update(cappedDelta)
            renderer.render(gameStateSnapshot())

            window.requestAnimationFrame { loop(it) }
        }

        window.requestAnimationFrame { loop(it) }
    }

    override fun update(deltaTimeMs: Double) {
        super.update(deltaTimeMs)

        if (isZoneActive) {
            freezeGauge -= zoneDrainPerMs * deltaTimeMs
            if (freezeGauge <= 0.0) deactivateZone()
        } else {
            if (freezeGauge > 0.0) freezeGauge -= PASSIVE_DRAIN_PER_MS * deltaTimeMs
        }
    }

    private fun activateZone() {
        isZoneActive = true
        val zoneDurationMs = (freezeGauge / MAX_GAUGE) * 8000.0
        zoneDrainPerMs = freezeGauge / zoneDurationMs
        EventOrchestrator.publish(FreezeTime(Double.MAX_VALUE, gameId))
        EventOrchestrator.publish(ZoneActivated(gameId))
    }

    private fun deactivateZone() {
        isZoneActive = false
        freezeGauge = 0.0
        zoneDrainPerMs = 0.0
        EventOrchestrator.publish(FreezeTime(Double.MIN_VALUE, gameId))
        EventOrchestrator.publish(ZoneDeactivated(gameId))
    }

    override fun reset() {
        super.reset()
        isZoneActive = false
        freezeGauge = 0.0
        zoneDrainPerMs = 0.0
    }
}