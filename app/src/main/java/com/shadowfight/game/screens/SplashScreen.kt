package com.shadowfight.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.FitViewport
import com.shadowfight.game.core.ShadowFightGame
import com.shadowfight.game.utils.GameConfig

/**
 * Splash screen shown at game startup
 */
class SplashScreen(private val game: ShadowFightGame) : Screen {

    private val camera = OrthographicCamera()
    private val viewport = FitViewport(GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT, camera)

    private var elapsedTime = 0f
    private val splashDuration = 2.5f

    override fun show() {
        camera.position.set(GameConfig.VIRTUAL_WIDTH / 2, GameConfig.VIRTUAL_HEIGHT / 2, 0f)
    }

    override fun render(delta: Float) {
        elapsedTime += delta

        // Clear screen with dark color
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.05f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()
        game.batch.projectionMatrix = camera.combined

        game.batch.begin()

        // Draw title with fade-in effect
        val alpha = minOf(1f, elapsedTime / 1f)
        game.assetManager.titleFont.color = Color(1f, 1f, 1f, alpha)

        val titleText = "SHADOW FIGHT 2"
        val glyphLayout = com.badlogic.gdx.graphics.g2d.GlyphLayout(game.assetManager.titleFont, titleText)
        val titleX = (GameConfig.VIRTUAL_WIDTH - glyphLayout.width) / 2
        val titleY = GameConfig.VIRTUAL_HEIGHT / 2 + 50

        game.assetManager.titleFont.draw(game.batch, titleText, titleX, titleY)

        // Draw subtitle
        if (elapsedTime > 0.5f) {
            val subAlpha = minOf(1f, (elapsedTime - 0.5f) / 0.5f)
            game.assetManager.uiFont.color = Color(0.7f, 0.7f, 0.7f, subAlpha)
            val subText = "Clone Edition"
            val subLayout = com.badlogic.gdx.graphics.g2d.GlyphLayout(game.assetManager.uiFont, subText)
            game.assetManager.uiFont.draw(
                game.batch,
                subText,
                (GameConfig.VIRTUAL_WIDTH - subLayout.width) / 2,
                GameConfig.VIRTUAL_HEIGHT / 2 - 20
            )
        }

        // Draw loading indicator
        if (elapsedTime > 1f) {
            game.assetManager.uiFont.color = Color(0.5f, 0.5f, 0.5f, 1f)
            val loadingText = "Loading..."
            val loadLayout = com.badlogic.gdx.graphics.g2d.GlyphLayout(game.assetManager.uiFont, loadingText)
            game.assetManager.uiFont.draw(
                game.batch,
                loadingText,
                (GameConfig.VIRTUAL_WIDTH - loadLayout.width) / 2,
                150f
            )
        }

        game.batch.end()

        // Transition to main menu after splash duration
        if (elapsedTime >= splashDuration) {
            game.goToMainMenu()
        }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
    override fun dispose() {}
}
