package webktris.view

data class GameMessage(val text: String, val timestamp: Double)

data class ClearAnimation(
    val rows: Collection<Int>,
    val startTime: Double,
    val duration: Double,
    val lineCount: Int,
    val particles: List<ClearParticle>
)

data class ClearParticle(val x: Double, val y: Double, val vx: Double, val vy: Double)