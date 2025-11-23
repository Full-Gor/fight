package com.shadowfight.game.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.controllers.Controller
import com.badlogic.gdx.controllers.ControllerListener
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.math.Vector2

/**
 * Advanced input manager for Desktop
 * Supports Keyboard, Mouse, and Gamepad controllers
 */
class DesktopInputManager : ControllerListener {

    // Current input state
    private val inputState = InputState()

    // Key bindings (customizable)
    var keyBindings = KeyBindings()

    // Controller settings
    var controllerDeadzone = 0.2f
    var controllerSensitivity = 1.0f

    // Connected controller
    private var activeController: Controller? = null

    // Mouse aiming (for ranged attacks)
    var mousePosition = Vector2()
    var isMouseAiming = false

    init {
        // Register controller listener
        try {
            Controllers.addListener(this)
            if (Controllers.getControllers().size > 0) {
                activeController = Controllers.getControllers().first()
                Gdx.app.log("Input", "Controller detected: ${activeController?.name}")
            }
        } catch (e: Exception) {
            Gdx.app.log("Input", "Controller support not available")
        }
    }

    /**
     * Update input state - call this every frame
     */
    fun update(): GameInput {
        // Reset input state
        inputState.reset()

        // Process keyboard input
        processKeyboardInput()

        // Process mouse input
        processMouseInput()

        // Process controller input
        processControllerInput()

        return inputState.toGameInput()
    }

    private fun processKeyboardInput() {
        // Movement
        if (Gdx.input.isKeyPressed(keyBindings.moveLeft)) {
            inputState.moveX = -1f
        }
        if (Gdx.input.isKeyPressed(keyBindings.moveRight)) {
            inputState.moveX = 1f
        }
        if (Gdx.input.isKeyPressed(keyBindings.crouch)) {
            inputState.crouch = true
        }

        // Jump
        if (Gdx.input.isKeyJustPressed(keyBindings.jump)) {
            inputState.jump = true
        }

        // Combat
        if (Gdx.input.isKeyJustPressed(keyBindings.punch)) {
            inputState.punch = true
        }
        if (Gdx.input.isKeyJustPressed(keyBindings.kick)) {
            inputState.kick = true
        }
        if (Gdx.input.isKeyPressed(keyBindings.block)) {
            inputState.block = true
        }

        // Special attacks
        if (Gdx.input.isKeyJustPressed(keyBindings.special1)) {
            inputState.special1 = true
        }
        if (Gdx.input.isKeyJustPressed(keyBindings.special2)) {
            inputState.special2 = true
        }
        if (Gdx.input.isKeyJustPressed(keyBindings.ranged)) {
            inputState.ranged = true
        }
        if (Gdx.input.isKeyJustPressed(keyBindings.magic)) {
            inputState.magic = true
        }

        // Dash (double tap or dedicated key)
        if (Gdx.input.isKeyJustPressed(keyBindings.dashLeft)) {
            inputState.dashLeft = true
        }
        if (Gdx.input.isKeyJustPressed(keyBindings.dashRight)) {
            inputState.dashRight = true
        }

        // Menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            inputState.pause = true
        }
    }

    private fun processMouseInput() {
        // Update mouse position
        mousePosition.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())

        // Mouse buttons for attacks (optional)
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            inputState.punch = true
        }
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            inputState.kick = true
        }
        if (Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
            inputState.block = true
        }

        // Mouse wheel for special moves
        val scrollY = Gdx.input.getDeltaY()
        if (scrollY != 0) {
            // Could be used for weapon switching or special selection
        }

        // Check if aiming with mouse (for ranged weapons)
        isMouseAiming = Gdx.input.isButtonPressed(Input.Buttons.RIGHT) &&
                        Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
    }

    private fun processControllerInput() {
        val controller = activeController ?: return

        // Left stick - movement
        val leftX = controller.getAxis(0) // Usually left stick X
        val leftY = controller.getAxis(1) // Usually left stick Y

        if (kotlin.math.abs(leftX) > controllerDeadzone) {
            inputState.moveX = leftX * controllerSensitivity
        }
        if (leftY > controllerDeadzone) {
            inputState.crouch = true
        }

        // Right stick - aiming (for ranged)
        val rightX = controller.getAxis(2)
        val rightY = controller.getAxis(3)
        if (kotlin.math.abs(rightX) > controllerDeadzone || kotlin.math.abs(rightY) > controllerDeadzone) {
            inputState.aimDirection.set(rightX, -rightY).nor()
        }

        // Face buttons (Xbox layout: A=0, B=1, X=2, Y=3)
        // PlayStation: Cross=0, Circle=1, Square=2, Triangle=3
        if (controller.getButton(0)) { // A / Cross - Jump
            inputState.jump = true
        }
        if (controller.getButton(2)) { // X / Square - Punch
            inputState.punch = true
        }
        if (controller.getButton(1)) { // B / Circle - Kick
            inputState.kick = true
        }
        if (controller.getButton(3)) { // Y / Triangle - Special
            inputState.special1 = true
        }

        // Shoulder buttons
        if (controller.getButton(4)) { // LB - Block
            inputState.block = true
        }
        if (controller.getButton(5)) { // RB - Ranged
            inputState.ranged = true
        }

        // Triggers (axes 4 and 5 on many controllers)
        val leftTrigger = controller.getAxis(4)
        val rightTrigger = controller.getAxis(5)

        if (leftTrigger > 0.5f) {
            inputState.dashLeft = true
        }
        if (rightTrigger > 0.5f) {
            inputState.dashRight = true
        }

        // D-Pad (usually buttons 11-14 or POV hat)
        // Some controllers report D-pad as axes
        if (controller.getButton(11)) inputState.moveX = -1f // D-Left
        if (controller.getButton(12)) inputState.moveX = 1f  // D-Right
        if (controller.getButton(13)) inputState.crouch = true // D-Down
        if (controller.getButton(14)) inputState.jump = true   // D-Up

        // Start/Options - Pause
        if (controller.getButton(7)) {
            inputState.pause = true
        }
    }

    // Controller listener callbacks
    override fun connected(controller: Controller) {
        if (activeController == null) {
            activeController = controller
            Gdx.app.log("Input", "Controller connected: ${controller.name}")
        }
    }

    override fun disconnected(controller: Controller) {
        if (controller == activeController) {
            activeController = null
            Gdx.app.log("Input", "Controller disconnected: ${controller.name}")

            // Try to find another controller
            if (Controllers.getControllers().size > 0) {
                activeController = Controllers.getControllers().first()
            }
        }
    }

    override fun buttonDown(controller: Controller, buttonCode: Int): Boolean = false
    override fun buttonUp(controller: Controller, buttonCode: Int): Boolean = false
    override fun axisMoved(controller: Controller, axisCode: Int, value: Float): Boolean = false

    fun dispose() {
        try {
            Controllers.removeListener(this)
        } catch (e: Exception) {
            // Ignore
        }
    }
}

/**
 * Customizable key bindings
 */
data class KeyBindings(
    // Movement
    var moveLeft: Int = Input.Keys.A,
    var moveRight: Int = Input.Keys.D,
    var jump: Int = Input.Keys.W,
    var crouch: Int = Input.Keys.S,

    // Combat
    var punch: Int = Input.Keys.J,
    var kick: Int = Input.Keys.K,
    var block: Int = Input.Keys.L,

    // Special moves
    var special1: Int = Input.Keys.U,
    var special2: Int = Input.Keys.I,
    var ranged: Int = Input.Keys.O,
    var magic: Int = Input.Keys.P,

    // Dash
    var dashLeft: Int = Input.Keys.Q,
    var dashRight: Int = Input.Keys.E,

    // Alternative arrow key controls
    var altMoveLeft: Int = Input.Keys.LEFT,
    var altMoveRight: Int = Input.Keys.RIGHT,
    var altJump: Int = Input.Keys.UP,
    var altCrouch: Int = Input.Keys.DOWN
)

/**
 * Internal input state tracking
 */
private class InputState {
    var moveX: Float = 0f
    var moveY: Float = 0f
    var jump: Boolean = false
    var crouch: Boolean = false
    var punch: Boolean = false
    var kick: Boolean = false
    var block: Boolean = false
    var special1: Boolean = false
    var special2: Boolean = false
    var ranged: Boolean = false
    var magic: Boolean = false
    var dashLeft: Boolean = false
    var dashRight: Boolean = false
    var pause: Boolean = false
    var aimDirection: Vector2 = Vector2()

    fun reset() {
        moveX = 0f
        moveY = 0f
        jump = false
        crouch = false
        punch = false
        kick = false
        block = false
        special1 = false
        special2 = false
        ranged = false
        magic = false
        dashLeft = false
        dashRight = false
        pause = false
        aimDirection.setZero()
    }

    fun toGameInput(): GameInput = GameInput(
        moveDirection = Vector2(moveX, moveY),
        jump = jump,
        crouch = crouch,
        punch = punch,
        kick = kick,
        block = block,
        special1 = special1,
        special2 = special2,
        ranged = ranged,
        magic = magic,
        dashLeft = dashLeft,
        dashRight = dashRight,
        pause = pause,
        aimDirection = aimDirection.cpy()
    )
}

/**
 * Game input data class
 */
data class GameInput(
    val moveDirection: Vector2 = Vector2(),
    val jump: Boolean = false,
    val crouch: Boolean = false,
    val punch: Boolean = false,
    val kick: Boolean = false,
    val block: Boolean = false,
    val special1: Boolean = false,
    val special2: Boolean = false,
    val ranged: Boolean = false,
    val magic: Boolean = false,
    val dashLeft: Boolean = false,
    val dashRight: Boolean = false,
    val pause: Boolean = false,
    val aimDirection: Vector2 = Vector2()
) {
    val hasMovement: Boolean get() = moveDirection.len() > 0.1f
    val hasAnyInput: Boolean get() = hasMovement || jump || crouch || punch || kick || block ||
            special1 || special2 || ranged || magic || dashLeft || dashRight
}
