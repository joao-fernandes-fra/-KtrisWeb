package webktris.view

import engine.model.KtrisContext
import engine.model.Piece
import engine.model.events.EventOrchestrator
import engine.model.events.GameEvent
import engine.model.events.InputEvent
import kotlinx.browser.window
import webktris.controller.GaugeChanged
import webktris.controller.GaugeFull
import webktris.controller.ZoneActivated
import webktris.controller.ZoneDeactivated
import webktris.model.RenderState

class GameEventSubscriber<T : Piece>(
    private val gameId: String,
    private val gameContext: KtrisContext<T>,
    private val state: RenderState,
    private val background: BackgroundRenderer
) {
    fun subscribe() {
        EventOrchestrator.subscribeForGameId<GameEvent.LevelUp>(gameId) { event ->
            state.currentLevel = event.newLevel
            state.levelUpFlashTime = window.performance.now()
            state.pendingLevelUpLevel = event.newLevel
            background.loadNext()
        }
        EventOrchestrator.subscribeForGameId<ZoneActivated>(gameId) {
            state.isZoneActive = true
        }
        EventOrchestrator.subscribeForGameId<ZoneDeactivated>(gameId) {
            state.isZoneActive = false
        }
        EventOrchestrator.subscribeForGameId<GameEvent.LineCleared>(gameId) { event ->
            if (event.linesCleared.isNotEmpty()) {
                state.linesToClear.add(event.linesCleared)
                if (event.isEmptyBoard) state.messageQueue.add(0, GameMessage("ALL CLEAR!", window.performance.now()))
            }
        }
        EventOrchestrator.subscribeForGameId<GaugeChanged>(gameId) { event ->
            state.gaugeValue = event.gaugeValue
        }

        EventOrchestrator.subscribeForGameId<GaugeFull>(gameId) {
            state.gaugeFullFlashTime = window.performance.now()
        }

        EventOrchestrator.subscribeForGameId<GameEvent.ScoreUpdated>(gameId) { event ->
            state.messageQueue.add(0, GameMessage(event.moveType.displayName, window.performance.now()))
        }

        EventOrchestrator.subscribeForGameId<GameEvent.ComboTriggered>(gameId) { event ->
            state.messageQueue.add(0, GameMessage("COMBO x${event.comboCount}", window.performance.now()))
        }

        EventOrchestrator.subscribeForGameId<GameEvent.BackToBackTrigger>(gameId) { event ->
            state.messageQueue.add(0, GameMessage("B2B x${event.backToBackCount}", window.performance.now()))
        }

        EventOrchestrator.subscribeForGameId<GameEvent.GameOver>(gameId) { event ->
            state.gameFinished = true
            state.finishMessage = if (event.goalMet) "VICTORY!" else "GAME OVER"
        }

        EventOrchestrator.subscribeForGameId<GameEvent.SpinDetected>(gameId) { event ->
            state.lastSpinType = event.spinType
            state.spinStartTime = window.performance.now()
        }
        EventOrchestrator.subscribeForGameId<InputEvent.ResetInput>(gameId) { _ ->
            state.gameFinished = false
            state.finishMessage = null
            state.isZoneActive = false
            state.currentLevel = 0
            state.gaugeValue = 0.0
            background.loadNext()
        }
    }
}