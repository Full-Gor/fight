package com.shadowfight.game

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.shadowfight.game.core.ShadowFightGame
import com.shadowfight.game.utils.GameConfig

/**
 * Desktop launcher for Shadow Fight 2 Clone
 * Allows testing and playing on PC
 */
fun main() {
    val config = Lwjgl3ApplicationConfiguration().apply {
        setTitle("Shadow Fight 2 Clone")
        setWindowedMode(1280, 720)
        setResizable(true)
        useVsync(true)
        setForegroundFPS(60)
        setWindowIcon("icon128.png", "icon64.png", "icon32.png", "icon16.png")
    }

    Lwjgl3Application(ShadowFightGame(), config)
}
