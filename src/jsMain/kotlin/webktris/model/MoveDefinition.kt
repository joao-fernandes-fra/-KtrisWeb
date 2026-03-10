package webktris.model

import engine.model.MoveType

data class MoveDefinition(
    override val id: String,
    override val displayName: String,
    override val isSpecial: Boolean,
    val basePoints: Double,
    val isDifficult: Boolean
) : MoveType

object UnknownMove : MoveType {
    override val id = "NONE"
    override val displayName = ""
    override val isSpecial = false
}