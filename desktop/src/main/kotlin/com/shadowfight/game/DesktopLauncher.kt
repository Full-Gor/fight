package com.shadowfight.game

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.shadowfight.game.core.ShadowFightGame

/**
 * Desktop launcher for Shadow Fight 2 Clone
 * Supports Keyboard, Mouse, and Gamepad controllers
 *
 * CONTROLS:
 *
 * === KEYBOARD ===
 * Movement:    A/D or Arrow Keys (Left/Right)
 * Jump:        W or Up Arrow
 * Crouch:      S or Down Arrow
 * Punch:       J or Left Mouse Button
 * Kick:        K or Right Mouse Button
 * Block:       L (hold) or Middle Mouse Button
 * Special 1:   U
 * Special 2:   I
 * Ranged:      O
 * Magic:       P
 * Dash Left:   Q
 * Dash Right:  E
 * Pause:       ESC or P
 *
 * === GAMEPAD (Xbox/PlayStation) ===
 * Movement:    Left Stick or D-Pad
 * Jump:        A / Cross
 * Punch:       X / Square
 * Kick:        B / Circle
 * Special:     Y / Triangle
 * Block:       LB / L1 (hold)
 * Ranged:      RB / R1
 * Dash:        LT/RT / L2/R2
 * Aim:         Right Stick
 * Pause:       Start / Options
 */
fun main() {
    val config = Lwjgl3ApplicationConfiguration().apply {
        setTitle("Shadow Fight 2 Clone - Desktop Edition")
        setWindowedMode(1280, 720)
        setResizable(true)
        useVsync(true)
        setForegroundFPS(60)
        setBackBufferConfig(8, 8, 8, 8, 16, 0, 4) // Anti-aliasing

        // Window settings
        setWindowSizeLimits(800, 450, 3840, 2160)
        setDecorated(true)

        // Try to set icons (won't crash if not found)
        try {
            setWindowIcon("icon128.png", "icon64.png", "icon32.png", "icon16.png")
        } catch (e: Exception) {
            // Icons not found, continue without them
        }
    }

    println("""
        ╔═══════════════════════════════════════════════════════════════╗
        ║          SHADOW FIGHT 2 CLONE - DESKTOP EDITION               ║
        ╠═══════════════════════════════════════════════════════════════╣
        ║  KEYBOARD CONTROLS:                                           ║
        ║  Movement: A/D or Arrows    Jump: W/Up    Crouch: S/Down     ║
        ║  Punch: J    Kick: K    Block: L (hold)    Pause: ESC        ║
        ║  Special: U/I    Ranged: O    Magic: P    Dash: Q/E          ║
        ╠═══════════════════════════════════════════════════════════════╣
        ║  GAMEPAD CONTROLS (Xbox/PlayStation):                         ║
        ║  Movement: Left Stick/D-Pad    Jump: A/Cross                 ║
        ║  Punch: X/Square    Kick: B/Circle    Block: LB/L1           ║
        ║  Special: Y/Triangle    Ranged: RB/R1    Dash: Triggers      ║
        ╠═══════════════════════════════════════════════════════════════╣
        ║  MOUSE: Left=Punch  Right=Kick  Middle=Block                 ║
        ╚═══════════════════════════════════════════════════════════════╝
    """.trimIndent())

    Lwjgl3Application(ShadowFightGame(), config)
}
