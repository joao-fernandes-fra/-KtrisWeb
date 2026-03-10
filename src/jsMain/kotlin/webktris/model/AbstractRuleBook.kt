package webktris.model

import engine.model.ScoringRuleBook
import engine.model.SpinType
import engine.model.SpinType.NONE

abstract class AbstractRuleBook : ScoringRuleBook {

    protected abstract val moveTable: Map<Pair<SpinType, Int>, MoveDefinition>

    fun resolve(action: SpinType, lines: Int): MoveDefinition? {
        moveTable[action to lines]?.let { return it }

        if (action == NONE && lines > 4) {
            val milestone = (lines / 4) * 4
            return moveTable[action to milestone]
        }

        return null
    }

    protected fun move(
        spinType: SpinType,
        lines: Int,
        id: String,
        displayName: String,
        points: Double,
        isSpecial: Boolean = false,
        isDifficult: Boolean = false
    ) = (spinType to lines) to MoveDefinition(id, displayName, isSpecial, points, isDifficult)
}