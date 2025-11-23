package com.shadowfight.game.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.shadowfight.game.utils.GameConfig

/**
 * Touch controls for mobile devices
 * Provides virtual joystick and action buttons
 */
class TouchControls(private val camera: OrthographicCamera) {

    // Virtual joystick
    private val joystickCenter = Vector2(200f, 250f)
    private val joystickRadius = 120f
    private val joystickKnobRadius = 50f
    private var joystickKnobPosition = Vector2(joystickCenter)
    private var joystickActive = false
    private var joystickPointer = -1

    // Action buttons
    private val punchButton = Circle(GameConfig.VIRTUAL_WIDTH - 350f, 200f, 70f)
    private val kickButton = Circle(GameConfig.VIRTUAL_WIDTH - 200f, 280f, 70f)
    private val blockButton = Circle(GameConfig.VIRTUAL_WIDTH - 200f, 120f, 60f)
    private val jumpButton = Circle(GameConfig.VIRTUAL_WIDTH - 450f, 320f, 60f)

    // Button states
    private var punchPressed = false
    private var kickPressed = false
    private var blockPressed = false
    private var jumpPressed = false

    // Touch tracking
    private val touchPos = Vector3()

    fun update(): TouchInput {
        // Reset button states
        var punch = false
        var kick = false
        var block = blockPressed // Block can be held
        var jump = false
        var crouch = false
        val moveDirection = Vector2()

        // Handle all active touches
        for (pointer in 0 until 10) {
            if (Gdx.input.isTouched(pointer)) {
                touchPos.set(Gdx.input.getX(pointer).toFloat(), Gdx.input.getY(pointer).toFloat(), 0f)
                camera.unproject(touchPos)

                // Check joystick
                if (pointer == joystickPointer || (!joystickActive && isInJoystickArea(touchPos.x, touchPos.y))) {
                    joystickActive = true
                    joystickPointer = pointer

                    // Calculate joystick direction
                    val dx = touchPos.x - joystickCenter.x
                    val dy = touchPos.y - joystickCenter.y
                    val distance = Vector2(dx, dy).len()

                    if (distance > joystickRadius) {
                        // Clamp to radius
                        val normalized = Vector2(dx, dy).nor()
                        joystickKnobPosition.set(
                            joystickCenter.x + normalized.x * joystickRadius,
                            joystickCenter.y + normalized.y * joystickRadius
                        )
                    } else {
                        joystickKnobPosition.set(touchPos.x, touchPos.y)
                    }

                    // Calculate move direction
                    val normalizedDx = (joystickKnobPosition.x - joystickCenter.x) / joystickRadius
                    val normalizedDy = (joystickKnobPosition.y - joystickCenter.y) / joystickRadius
                    moveDirection.set(normalizedDx, normalizedDy)

                    // Crouch detection (pulling down on joystick)
                    crouch = normalizedDy < -0.5f
                }

                // Check action buttons (only on just touched)
                if (Gdx.input.justTouched() || !wasButtonPressed(pointer)) {
                    if (punchButton.contains(touchPos.x, touchPos.y)) {
                        punch = true
                        punchPressed = true
                    }
                    if (kickButton.contains(touchPos.x, touchPos.y)) {
                        kick = true
                        kickPressed = true
                    }
                    if (jumpButton.contains(touchPos.x, touchPos.y)) {
                        jump = true
                        jumpPressed = true
                    }
                }

                // Block can be held
                if (blockButton.contains(touchPos.x, touchPos.y)) {
                    block = true
                    blockPressed = true
                }
            }
        }

        // Reset joystick if not touched
        if (!Gdx.input.isTouched(joystickPointer)) {
            joystickActive = false
            joystickPointer = -1
            joystickKnobPosition.set(joystickCenter)
        }

        // Reset button states
        punchPressed = false
        kickPressed = false
        jumpPressed = false
        if (!isBlockButtonTouched()) {
            blockPressed = false
        }

        return TouchInput(
            moveDirection = moveDirection,
            punch = punch,
            kick = kick,
            block = block,
            jump = jump,
            crouch = crouch
        )
    }

    private fun isInJoystickArea(x: Float, y: Float): Boolean {
        val distance = Vector2(x - joystickCenter.x, y - joystickCenter.y).len()
        return distance < joystickRadius * 1.5f
    }

    private fun wasButtonPressed(pointer: Int): Boolean {
        return punchPressed || kickPressed || jumpPressed || blockPressed
    }

    private fun isBlockButtonTouched(): Boolean {
        for (pointer in 0 until 10) {
            if (Gdx.input.isTouched(pointer)) {
                touchPos.set(Gdx.input.getX(pointer).toFloat(), Gdx.input.getY(pointer).toFloat(), 0f)
                camera.unproject(touchPos)
                if (blockButton.contains(touchPos.x, touchPos.y)) {
                    return true
                }
            }
        }
        return false
    }

    fun resize(width: Int, height: Int) {
        // Adjust button positions if needed
    }

    fun render(shapeRenderer: ShapeRenderer, batch: SpriteBatch) {
        shapeRenderer.projectionMatrix = camera.combined

        // Draw joystick base
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Joystick outer ring
        shapeRenderer.color = Color(0.3f, 0.3f, 0.4f, 0.4f)
        shapeRenderer.circle(joystickCenter.x, joystickCenter.y, joystickRadius)

        // Joystick knob
        val knobColor = if (joystickActive) Color(0.5f, 0.5f, 0.7f, 0.7f) else Color(0.4f, 0.4f, 0.5f, 0.6f)
        shapeRenderer.color = knobColor
        shapeRenderer.circle(joystickKnobPosition.x, joystickKnobPosition.y, joystickKnobRadius)

        // Punch button (red)
        shapeRenderer.color = Color(0.8f, 0.2f, 0.2f, 0.6f)
        shapeRenderer.circle(punchButton.x, punchButton.y, punchButton.radius)

        // Kick button (blue)
        shapeRenderer.color = Color(0.2f, 0.4f, 0.8f, 0.6f)
        shapeRenderer.circle(kickButton.x, kickButton.y, kickButton.radius)

        // Block button (yellow)
        shapeRenderer.color = Color(0.8f, 0.7f, 0.2f, 0.6f)
        shapeRenderer.circle(blockButton.x, blockButton.y, blockButton.radius)

        // Jump button (green)
        shapeRenderer.color = Color(0.2f, 0.7f, 0.3f, 0.6f)
        shapeRenderer.circle(jumpButton.x, jumpButton.y, jumpButton.radius)

        shapeRenderer.end()

        // Draw button outlines
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        Gdx.gl.glLineWidth(3f)

        shapeRenderer.color = Color(1f, 0.3f, 0.3f, 0.8f)
        shapeRenderer.circle(punchButton.x, punchButton.y, punchButton.radius)

        shapeRenderer.color = Color(0.3f, 0.5f, 1f, 0.8f)
        shapeRenderer.circle(kickButton.x, kickButton.y, kickButton.radius)

        shapeRenderer.color = Color(1f, 0.9f, 0.3f, 0.8f)
        shapeRenderer.circle(blockButton.x, blockButton.y, blockButton.radius)

        shapeRenderer.color = Color(0.3f, 0.9f, 0.4f, 0.8f)
        shapeRenderer.circle(jumpButton.x, jumpButton.y, jumpButton.radius)

        shapeRenderer.color = Color(0.5f, 0.5f, 0.6f, 0.8f)
        shapeRenderer.circle(joystickCenter.x, joystickCenter.y, joystickRadius)

        shapeRenderer.end()

        // Draw button labels
        batch.projectionMatrix = camera.combined
        batch.begin()
        // Labels would be drawn here with font
        batch.end()
    }
}

/**
 * Data class for touch input state
 */
data class TouchInput(
    val moveDirection: Vector2 = Vector2(),
    val punch: Boolean = false,
    val kick: Boolean = false,
    val block: Boolean = false,
    val jump: Boolean = false,
    val crouch: Boolean = false
)
