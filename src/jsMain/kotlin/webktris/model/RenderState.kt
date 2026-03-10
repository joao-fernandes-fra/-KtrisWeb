package webktris.model

import engine.model.SpinType
import webktris.view.ClearAnimation
import webktris.view.GameMessage

class RenderState {
    var currentLevel = 0
    var levelUpFlashTime = -10_000.0
    var gameFinished = false
    var finishMessage: String? = null
    var lastSpinType: SpinType? = null
    var spinStartTime = 0.0
    var pendingLevelUpLevel: Int? = null
    var gaugeValue = 0.0
        set(v) {
            field = v.coerceIn(0.0, 40.0)
        }
    var gaugeFullFlashTime = -10_000.0
    val messageQueue = mutableListOf<GameMessage>()
    val linesToClear = mutableListOf<Collection<Int>>()
    val activeClearEffects = mutableSetOf<ClearAnimation>()
    var isZoneActive = false

}