package com.shadowfight.game.assets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Disposable

/**
 * Manages all game assets including textures, animations, sounds, and fonts
 */
class AssetManager : Disposable {

    // Textures
    private val textures = mutableMapOf<String, Texture>()
    private val animations = mutableMapOf<String, Animation<TextureRegion>>()

    // Audio
    private val sounds = mutableMapOf<String, Sound>()
    private var backgroundMusic: Music? = null

    // Fonts
    lateinit var titleFont: BitmapFont
        private set
    lateinit var uiFont: BitmapFont
        private set
    lateinit var damageFont: BitmapFont
        private set

    // Generated textures for silhouettes
    lateinit var playerSilhouette: Texture
        private set
    lateinit var enemySilhouette: Texture
        private set
    lateinit var backgroundTexture: Texture
        private set

    fun loadInitialAssets() {
        Gdx.app.log("AssetManager", "Loading initial assets...")

        // Generate placeholder textures (silhouette style like Shadow Fight)
        generateSilhouetteTextures()
        generateBackgroundTexture()

        // Create fonts
        createFonts()

        Gdx.app.log("AssetManager", "Assets loaded successfully")
    }

    private fun generateSilhouetteTextures() {
        // Player silhouette (dark shadow figure)
        playerSilhouette = createSilhouetteTexture(128, 256, Color(0.05f, 0.05f, 0.1f, 1f))

        // Enemy silhouette (slightly different shade)
        enemySilhouette = createSilhouetteTexture(128, 256, Color(0.1f, 0.05f, 0.05f, 1f))
    }

    private fun createSilhouetteTexture(width: Int, height: Int, color: Color): Texture {
        val pixmap = Pixmap(width, height, Pixmap.Format.RGBA8888)

        // Create humanoid silhouette shape
        pixmap.setColor(color)

        // Head (circle)
        val headRadius = width / 5
        val headY = height - headRadius - 10
        pixmap.fillCircle(width / 2, headY, headRadius)

        // Neck
        pixmap.fillRectangle(width / 2 - 8, headY - headRadius, 16, 20)

        // Torso (trapezoid approximation)
        val torsoTop = headY - headRadius - 20
        val torsoBottom = height / 2
        for (y in torsoTop downTo torsoBottom) {
            val progress = (torsoTop - y).toFloat() / (torsoTop - torsoBottom)
            val halfWidth = (15 + progress * 20).toInt()
            pixmap.fillRectangle(width / 2 - halfWidth, y, halfWidth * 2, 1)
        }

        // Arms (rectangles at angles - simplified)
        // Left arm
        pixmap.fillRectangle(width / 2 - 45, torsoTop - 10, 35, 12)
        pixmap.fillRectangle(width / 2 - 55, torsoTop - 5, 12, 50)

        // Right arm
        pixmap.fillRectangle(width / 2 + 10, torsoTop - 10, 35, 12)
        pixmap.fillRectangle(width / 2 + 43, torsoTop - 5, 12, 50)

        // Legs
        val legTop = torsoBottom
        val legBottom = 10

        // Left leg
        pixmap.fillRectangle(width / 2 - 25, legBottom, 18, legTop - legBottom)

        // Right leg
        pixmap.fillRectangle(width / 2 + 7, legBottom, 18, legTop - legBottom)

        val texture = Texture(pixmap)
        pixmap.dispose()
        return texture
    }

    private fun generateBackgroundTexture() {
        val width = 1920
        val height = 1080
        val pixmap = Pixmap(width, height, Pixmap.Format.RGBA8888)

        // Gradient background (dark atmosphere like Shadow Fight 2)
        for (y in 0 until height) {
            val progress = y.toFloat() / height
            val r = (0.1f + progress * 0.15f)
            val g = (0.05f + progress * 0.1f)
            val b = (0.15f + progress * 0.2f)
            pixmap.setColor(r, g, b, 1f)
            pixmap.drawLine(0, y, width, y)
        }

        // Add some atmospheric elements (distant mountains/structures)
        pixmap.setColor(0.08f, 0.04f, 0.12f, 1f)

        // Mountains silhouette
        val mountainPoints = intArrayOf(
            0, 400,
            200, 250,
            400, 350,
            600, 200,
            800, 300,
            1000, 150,
            1200, 280,
            1400, 180,
            1600, 320,
            1920, 380
        )

        for (i in 0 until mountainPoints.size - 2 step 2) {
            val x1 = mountainPoints[i]
            val y1 = mountainPoints[i + 1]
            val x2 = mountainPoints[i + 2]
            val y2 = mountainPoints[i + 3]

            for (x in x1 until x2) {
                val t = (x - x1).toFloat() / (x2 - x1)
                val y = (y1 + t * (y2 - y1)).toInt()
                pixmap.fillRectangle(x, 0, 1, y)
            }
        }

        // Ground
        pixmap.setColor(0.03f, 0.02f, 0.05f, 1f)
        pixmap.fillRectangle(0, 0, width, 150)

        // Ground line glow
        for (i in 0..20) {
            val alpha = 1f - (i / 20f)
            pixmap.setColor(0.3f, 0.1f, 0.4f, alpha * 0.3f)
            pixmap.drawLine(0, 150 + i, width, 150 + i)
        }

        backgroundTexture = Texture(pixmap)
        pixmap.dispose()
    }

    private fun createFonts() {
        // Use default font as placeholder
        titleFont = BitmapFont().apply {
            data.setScale(3f)
            color = Color.WHITE
        }

        uiFont = BitmapFont().apply {
            data.setScale(1.5f)
            color = Color.WHITE
        }

        damageFont = BitmapFont().apply {
            data.setScale(2f)
            color = Color.RED
        }
    }

    fun getTexture(name: String): Texture? = textures[name]

    fun getAnimation(name: String): Animation<TextureRegion>? = animations[name]

    fun playSound(name: String) {
        sounds[name]?.play()
    }

    fun playMusic(name: String, loop: Boolean = true) {
        backgroundMusic?.stop()
        // Music loading would go here
    }

    fun stopMusic() {
        backgroundMusic?.stop()
    }

    override fun dispose() {
        textures.values.forEach { it.dispose() }
        sounds.values.forEach { it.dispose() }
        backgroundMusic?.dispose()
        titleFont.dispose()
        uiFont.dispose()
        damageFont.dispose()
        playerSilhouette.dispose()
        enemySilhouette.dispose()
        backgroundTexture.dispose()
    }
}
