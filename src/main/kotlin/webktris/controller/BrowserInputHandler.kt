package webktris.controller

import engine.model.Drop
import engine.model.Movement
import engine.model.Rotation
import engine.model.events.EventOrchestrator
import engine.model.events.InputEvent
import kotlinx.browser.window
import org.w3c.dom.events.KeyboardEvent

class BrowserInputHandler(private val gameId: String) {
    private val activeKeys = mutableSetOf<String>()

    init {
        window.addEventListener("keydown", { e -> onKeyDown(e as KeyboardEvent) })
        window.addEventListener("keyup", { e -> onKeyUp(e as KeyboardEvent) })
    }

    private fun onKeyDown(e: KeyboardEvent) {
        val preventKeys = setOf("ArrowUp", "ArrowDown", "ArrowLeft", "ArrowRight", " ")
        if (preventKeys.contains(e.key)) e.preventDefault()

        if (activeKeys.add(e.code)) {
            when (e.code) {
                "Space" -> EventOrchestrator.publish(InputEvent.DropInput(Drop.HARD_DROP, gameId))
                "KeyZ" -> EventOrchestrator.publish(InputEvent.RotationInputStart(Rotation.ROTATE_CCW, gameId))
                "KeyX" -> EventOrchestrator.publish(InputEvent.RotationInputStart(Rotation.ROTATE_CW, gameId))
                "ArrowUp" -> EventOrchestrator.publish(InputEvent.RotationInputStart(Rotation.ROTATE_180, gameId))
                "KeyC" -> EventOrchestrator.publish(InputEvent.HoldInput(gameId))
                "ArrowDown" -> EventOrchestrator.publish(InputEvent.DropInput(Drop.SOFT_DROP, gameId))
                "ArrowLeft" -> EventOrchestrator.publish(InputEvent.DirectionMoveStart(Movement.MOVE_LEFT, gameId))
                "ArrowRight" -> EventOrchestrator.publish(InputEvent.DirectionMoveStart(Movement.MOVE_RIGHT, gameId))
                "KeyS" -> EventOrchestrator.publish(ZoneInput(gameId))
                "KeyR" -> EventOrchestrator.publish(InputEvent.ResetInput(gameId))
            }
        }
    }

    private fun onKeyUp(e: KeyboardEvent) {
        if (activeKeys.remove(e.code)) {
            when (e.code) {
                "ArrowUp" -> EventOrchestrator.publish(InputEvent.RotationInputRelease(Rotation.ROTATE_180, gameId))
                "ArrowLeft" -> EventOrchestrator.publish(InputEvent.DirectionMoveEnd(Movement.MOVE_LEFT, gameId))
                "ArrowRight" -> EventOrchestrator.publish(InputEvent.DirectionMoveEnd(Movement.MOVE_RIGHT, gameId))
                "KeyZ" -> EventOrchestrator.publish(InputEvent.RotationInputRelease(Rotation.ROTATE_CCW, gameId))
                "KeyX" -> EventOrchestrator.publish(InputEvent.RotationInputRelease(Rotation.ROTATE_CW, gameId))
            }
        }
    }
}
