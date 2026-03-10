package webktris.model

import engine.model.Drop
import engine.model.MoveType
import engine.model.SpinType
import engine.model.SpinType.*

class ExtendedRuleBook : AbstractRuleBook() {

    override val perfectClearBonus = 4000.0
    override val comboFactor = 75.0
    override val dropTables = mapOf(
        Drop.SOFT_DROP to 1.0,
        Drop.HARD_DROP to 2.0
    )

    override fun getBasePoints(action: SpinType, lines: Int): Double {
        return resolve(action, lines)?.basePoints ?: 0.0
    }

    override fun getMoveType(action: SpinType, lines: Int): MoveType {
        return resolve(action, lines) ?: UnknownMove
    }

    override fun isDifficult(action: SpinType, lines: Int): Boolean {
        return resolve(action, lines)?.isDifficult ?: false
    }

    override val moveTable = mapOf(
        move(NONE, 1, "SINGLE", "Single", 100.0),
        move(NONE, 2, "DOUBLE", "Double", 300.0),
        move(NONE, 3, "TRIPLE", "Triple", 500.0),

        move(NONE, 4, "QUAD", "Quad", 800.0, isSpecial = true, isDifficult = true),
        move(NONE, 8, "OCTA", "Octa", 3000.0, isSpecial = true, isDifficult = true),
        move(NONE, 12, "DODECA", "Dodeca", 7000.0, isSpecial = true, isDifficult = true),
        move(NONE, 16, "HEXA", "Hexa", 13200.0, isSpecial = true, isDifficult = true),
        move(NONE, 20, "PERFECT", "Perfect", 21000.0, isSpecial = true, isDifficult = true),
        move(NONE, 24, "IMPOSSIBLE", "Impossible", 30400.0, isSpecial = true, isDifficult = true),

        move(FULL, 0, "SPIN_ZERO", "Spin", 400.0, isSpecial = true, isDifficult = true),
        move(FULL, 1, "SPIN_SINGLE", "Spin Single", 800.0, isSpecial = true, isDifficult = true),
        move(FULL, 2, "SPIN_DOUBLE", "Spin Double", 1200.0, isSpecial = true, isDifficult = true),
        move(FULL, 3, "SPIN_TRIPLE", "Spin Triple", 1600.0, isSpecial = true, isDifficult = true),

        move(MINI, 0, "MINI_SPIN", "Mini Spin", 100.0, isSpecial = true),
        move(MINI, 1, "MINI_SPIN_ONE", "Mini Spin Single", 200.0, isSpecial = true),
        move(MINI, 2, "MINI_SPIN_TWO", "Mini Spin Double", 400.0, isSpecial = true),
    )
}