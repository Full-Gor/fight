package com.shadowfight.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.FitViewport
import com.shadowfight.game.core.ShadowFightGame
import com.shadowfight.game.utils.GameConfig

/**
 * Main menu screen with game options
 */
class MainMenuScreen(private val game: ShadowFightGame) : Screen {

    private val camera = OrthographicCamera()
    private val viewport = FitViewport(GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT, camera)
    private val touchPoint = Vector3()

    // Menu buttons
    private val buttons = mutableListOf<MenuButton>()
    private var selectedButton = 0
    private var animationTime = 0f

    init {
        val buttonWidth = 400f
        val buttonHeight = 80f
        val startY = GameConfig.VIRTUAL_HEIGHT / 2 + 50f
        val spacing = 100f
        val centerX = (GameConfig.VIRTUAL_WIDTH - buttonWidth) / 2

        buttons.add(MenuButton("FIGHT", Rectangle(centerX, startY, buttonWidth, buttonHeight)) {
            game.startFight()
        })
        buttons.add(MenuButton("TOURNAMENT", Rectangle(centerX, startY - spacing, buttonWidth, buttonHeight)) {
            game.startFight(com.shadowfight.game.core.EnemyType.TOURNAMENT)
        })
        buttons.add(MenuButton("EQUIPMENT", Rectangle(centerX, startY - spacing * 2, buttonWidth, buttonHeight)) {
            game.goToEquipment()
        })
        buttons.add(MenuButton("ACTS", Rectangle(centerX, startY - spacing * 3, buttonWidth, buttonHeight)) {
            game.goToActSelection()
        })
        buttons.add(MenuButton("EXIT", Rectangle(centerX, startY - spacing * 4, buttonWidth, buttonHeight)) {
            Gdx.app.exit()
        })
    }

    override fun show() {
        camera.position.set(GameConfig.VIRTUAL_WIDTH / 2, GameConfig.VIRTUAL_HEIGHT / 2, 0f)
    }

    override fun render(delta: Float) {
        animationTime += delta

        handleInput()

        // Clear screen
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.05f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()

        // Draw background
        game.batch.projectionMatrix = camera.combined
        game.batch.begin()
        game.batch.draw(game.assetManager.backgroundTexture, 0f, 0f, GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT)
        game.batch.end()

        // Draw buttons with shape renderer
        game.shapeRenderer.projectionMatrix = camera.combined
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        buttons.forEachIndexed { index, button ->
            val isSelected = index == selectedButton
            val color = if (isSelected) {
                Color(0.3f, 0.1f, 0.4f, 0.9f)
            } else {
                Color(0.1f, 0.05f, 0.15f, 0.8f)
            }
            game.shapeRenderer.color = color
            game.shapeRenderer.rect(button.bounds.x, button.bounds.y, button.bounds.width, button.bounds.height)

            // Highlight border for selected
            if (isSelected) {
                game.shapeRenderer.color = Color(0.6f, 0.2f, 0.8f, 1f)
                val pulse = (kotlin.math.sin(animationTime * 5) * 0.3f + 0.7f).toFloat()
                game.shapeRenderer.color = Color(pulse, 0.2f * pulse, pulse, 1f)
            }
        }

        game.shapeRenderer.end()

        // Draw button borders
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        Gdx.gl.glLineWidth(3f)
        buttons.forEachIndexed { index, button ->
            val isSelected = index == selectedButton
            game.shapeRenderer.color = if (isSelected) Color(0.8f, 0.3f, 1f, 1f) else Color(0.4f, 0.2f, 0.5f, 1f)
            game.shapeRenderer.rect(button.bounds.x, button.bounds.y, button.bounds.width, button.bounds.height)
        }
        game.shapeRenderer.end()

        // Draw text
        game.batch.begin()

        // Title
        game.assetManager.titleFont.color = Color.WHITE
        val titleText = "SHADOW FIGHT 2"
        val titleLayout = GlyphLayout(game.assetManager.titleFont, titleText)
        game.assetManager.titleFont.draw(
            game.batch,
            titleText,
            (GameConfig.VIRTUAL_WIDTH - titleLayout.width) / 2,
            GameConfig.VIRTUAL_HEIGHT - 80f
        )

        // Button labels
        buttons.forEachIndexed { index, button ->
            val isSelected = index == selectedButton
            game.assetManager.uiFont.color = if (isSelected) Color.WHITE else Color(0.7f, 0.7f, 0.7f, 1f)
            val layout = GlyphLayout(game.assetManager.uiFont, button.text)
            game.assetManager.uiFont.draw(
                game.batch,
                button.text,
                button.bounds.x + (button.bounds.width - layout.width) / 2,
                button.bounds.y + (button.bounds.height + layout.height) / 2
            )
        }

        // Player info
        game.assetManager.uiFont.color = Color(0.8f, 0.8f, 0.2f, 1f)
        game.assetManager.uiFont.draw(game.batch, "Level: ${game.playerData.level}", 50f, 100f)
        game.assetManager.uiFont.draw(game.batch, "Coins: ${game.playerData.coins}", 50f, 60f)
        game.assetManager.uiFont.draw(game.batch, "Gems: ${game.playerData.gems}", 250f, 60f)

        game.batch.end()
    }

    private fun handleInput() {
        // Keyboard navigation
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedButton = (selectedButton - 1 + buttons.size) % buttons.size
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedButton = (selectedButton + 1) % buttons.size
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            buttons[selectedButton].action()
        }

        // Touch/mouse input
        if (Gdx.input.justTouched()) {
            touchPoint.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            camera.unproject(touchPoint)

            buttons.forEachIndexed { index, button ->
                if (button.bounds.contains(touchPoint.x, touchPoint.y)) {
                    selectedButton = index
                    button.action()
                }
            }
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

data class MenuButton(
    val text: String,
    val bounds: Rectangle,
    val action: () -> Unit
)
