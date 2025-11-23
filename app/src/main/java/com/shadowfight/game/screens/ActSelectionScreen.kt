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
import com.shadowfight.game.core.EnemyType
import com.shadowfight.game.core.ShadowFightGame
import com.shadowfight.game.utils.GameConfig

/**
 * Act selection screen for story mode progression
 */
class ActSelectionScreen(private val game: ShadowFightGame) : Screen {

    private val camera = OrthographicCamera()
    private val viewport = FitViewport(GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT, camera)
    private val touchPoint = Vector3()

    private var selectedAct = game.playerData.currentAct - 1
    private val actButtons = mutableListOf<ActButton>()
    private val backButton = Rectangle(50f, 50f, 150f, 60f)
    private val fightBossButton = Rectangle(GameConfig.VIRTUAL_WIDTH - 350f, 150f, 300f, 80f)

    // Act data
    private val acts = listOf(
        ActData(1, "Act I: Hermit", "The Hermit", "Master of the forest", Color(0.2f, 0.6f, 0.3f, 1f), 5),
        ActData(2, "Act II: Butcher", "The Butcher", "Savage meat cleaver", Color(0.6f, 0.2f, 0.2f, 1f), 10),
        ActData(3, "Act III: Wasp", "The Wasp", "Swift and deadly", Color(0.8f, 0.7f, 0.1f, 1f), 15),
        ActData(4, "Act IV: Widow", "The Widow", "Mistress of shadows", Color(0.3f, 0.1f, 0.4f, 1f), 20),
        ActData(5, "Act V: Shogun", "The Shogun", "Ancient warrior", Color(0.7f, 0.5f, 0.2f, 1f), 25),
        ActData(6, "Act VI: Titan", "Titan", "Immortal giant", Color(0.4f, 0.4f, 0.5f, 1f), 30),
        ActData(7, "Act VII: Gates", "Gates of Shadows", "The final battle", Color(0.1f, 0.1f, 0.2f, 1f), 35)
    )

    init {
        val buttonWidth = 220f
        val buttonHeight = 120f
        val startX = 100f
        val startY = GameConfig.VIRTUAL_HEIGHT - 350f
        val spacing = 240f

        acts.forEachIndexed { index, act ->
            val x = startX + (index % 4) * spacing
            val y = startY - (index / 4) * 150f
            actButtons.add(ActButton(act, Rectangle(x, y, buttonWidth, buttonHeight)))
        }
    }

    override fun show() {
        camera.position.set(GameConfig.VIRTUAL_WIDTH / 2, GameConfig.VIRTUAL_HEIGHT / 2, 0f)
    }

    override fun render(delta: Float) {
        handleInput()

        Gdx.gl.glClearColor(0.03f, 0.02f, 0.05f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()
        game.batch.projectionMatrix = camera.combined
        game.shapeRenderer.projectionMatrix = camera.combined

        // Draw background
        game.batch.begin()
        game.batch.draw(game.assetManager.backgroundTexture, 0f, 0f, GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT)
        game.batch.end()

        drawActButtons()
        drawSelectedActInfo()
        drawUI()
    }

    private fun handleInput() {
        // Keyboard navigation
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            selectedAct = maxOf(0, selectedAct - 1)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            selectedAct = minOf(acts.size - 1, selectedAct + 1)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            startFight()
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.goToMainMenu()
        }

        // Touch input
        if (Gdx.input.justTouched()) {
            touchPoint.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            camera.unproject(touchPoint)

            // Check act buttons
            actButtons.forEachIndexed { index, button ->
                if (button.bounds.contains(touchPoint.x, touchPoint.y)) {
                    if (selectedAct == index && isActUnlocked(index + 1)) {
                        startFight()
                    } else {
                        selectedAct = index
                    }
                }
            }

            // Check fight boss button
            if (fightBossButton.contains(touchPoint.x, touchPoint.y) && isActUnlocked(selectedAct + 1)) {
                startBossFight()
            }

            // Check back button
            if (backButton.contains(touchPoint.x, touchPoint.y)) {
                game.goToMainMenu()
            }
        }
    }

    private fun isActUnlocked(actNumber: Int): Boolean {
        return actNumber <= game.playerData.currentAct
    }

    private fun startFight() {
        if (isActUnlocked(selectedAct + 1)) {
            game.startFight(EnemyType.REGULAR, selectedAct + 1)
        }
    }

    private fun startBossFight() {
        if (isActUnlocked(selectedAct + 1)) {
            game.startFight(EnemyType.BOSS, selectedAct + 1)
        }
    }

    private fun drawActButtons() {
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        actButtons.forEachIndexed { index, button ->
            val isSelected = index == selectedAct
            val isUnlocked = isActUnlocked(index + 1)
            val act = button.actData

            // Background color
            val bgColor = if (isUnlocked) {
                if (isSelected) act.color.cpy().mul(1.3f) else act.color.cpy().mul(0.6f)
            } else {
                Color(0.2f, 0.2f, 0.2f, 0.5f)
            }
            game.shapeRenderer.color = bgColor
            game.shapeRenderer.rect(button.bounds.x, button.bounds.y, button.bounds.width, button.bounds.height)

            // Selected indicator
            if (isSelected) {
                game.shapeRenderer.color = Color.WHITE
                game.shapeRenderer.rectLine(
                    button.bounds.x, button.bounds.y,
                    button.bounds.x + button.bounds.width, button.bounds.y, 3f
                )
                game.shapeRenderer.rectLine(
                    button.bounds.x, button.bounds.y + button.bounds.height,
                    button.bounds.x + button.bounds.width, button.bounds.y + button.bounds.height, 3f
                )
            }
        }

        game.shapeRenderer.end()

        // Draw outlines
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        Gdx.gl.glLineWidth(2f)

        actButtons.forEachIndexed { index, button ->
            val isUnlocked = isActUnlocked(index + 1)
            game.shapeRenderer.color = if (isUnlocked) button.actData.color else Color.DARK_GRAY
            game.shapeRenderer.rect(button.bounds.x, button.bounds.y, button.bounds.width, button.bounds.height)
        }

        game.shapeRenderer.end()

        // Draw text
        game.batch.begin()

        actButtons.forEachIndexed { index, button ->
            val isUnlocked = isActUnlocked(index + 1)
            val act = button.actData

            // Act number
            game.assetManager.uiFont.color = if (isUnlocked) Color.WHITE else Color.GRAY
            game.assetManager.uiFont.draw(
                game.batch,
                "ACT ${act.number}",
                button.bounds.x + 10f,
                button.bounds.y + button.bounds.height - 10f
            )

            // Boss name
            val bossText = if (isUnlocked) act.bossName else "???"
            game.assetManager.uiFont.draw(
                game.batch,
                bossText,
                button.bounds.x + 10f,
                button.bounds.y + 40f
            )

            // Lock icon for locked acts
            if (!isUnlocked) {
                game.assetManager.uiFont.color = Color.RED
                game.assetManager.uiFont.draw(
                    game.batch,
                    "LOCKED",
                    button.bounds.x + 60f,
                    button.bounds.y + 70f
                )
            }
        }

        game.batch.end()
    }

    private fun drawSelectedActInfo() {
        val selectedActData = acts[selectedAct]
        val isUnlocked = isActUnlocked(selectedAct + 1)

        // Info panel background
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        game.shapeRenderer.color = Color(0.1f, 0.08f, 0.15f, 0.9f)
        game.shapeRenderer.rect(GameConfig.VIRTUAL_WIDTH - 450f, 100f, 400f, 400f)
        game.shapeRenderer.end()

        game.batch.begin()

        // Act title
        game.assetManager.titleFont.color = selectedActData.color
        game.assetManager.titleFont.draw(
            game.batch,
            selectedActData.name,
            GameConfig.VIRTUAL_WIDTH - 430f,
            470f
        )

        if (isUnlocked) {
            // Boss info
            game.assetManager.uiFont.color = Color.WHITE
            game.assetManager.uiFont.draw(game.batch, "Boss: ${selectedActData.bossName}", GameConfig.VIRTUAL_WIDTH - 430f, 400f)
            game.assetManager.uiFont.draw(game.batch, selectedActData.bossDescription, GameConfig.VIRTUAL_WIDTH - 430f, 360f)

            // Recommended level
            game.assetManager.uiFont.color = Color.YELLOW
            game.assetManager.uiFont.draw(game.batch, "Recommended Lv: ${selectedActData.recommendedLevel}", GameConfig.VIRTUAL_WIDTH - 430f, 300f)

            // Player level comparison
            val playerLevel = game.playerData.level
            game.assetManager.uiFont.color = if (playerLevel >= selectedActData.recommendedLevel) Color.GREEN else Color.RED
            game.assetManager.uiFont.draw(game.batch, "Your Level: $playerLevel", GameConfig.VIRTUAL_WIDTH - 430f, 260f)
        } else {
            game.assetManager.uiFont.color = Color.RED
            game.assetManager.uiFont.draw(game.batch, "Complete previous act to unlock!", GameConfig.VIRTUAL_WIDTH - 430f, 350f)
        }

        game.batch.end()

        // Fight boss button
        if (isUnlocked) {
            game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            game.shapeRenderer.color = Color(0.6f, 0.2f, 0.2f, 0.9f)
            game.shapeRenderer.rect(fightBossButton.x, fightBossButton.y, fightBossButton.width, fightBossButton.height)
            game.shapeRenderer.end()

            game.batch.begin()
            game.assetManager.uiFont.color = Color.WHITE
            val fightText = "FIGHT BOSS"
            val layout = GlyphLayout(game.assetManager.uiFont, fightText)
            game.assetManager.uiFont.draw(
                game.batch,
                fightText,
                fightBossButton.x + (fightBossButton.width - layout.width) / 2,
                fightBossButton.y + (fightBossButton.height + layout.height) / 2
            )
            game.batch.end()
        }
    }

    private fun drawUI() {
        // Title
        game.batch.begin()
        game.assetManager.titleFont.color = Color.WHITE
        game.assetManager.titleFont.draw(game.batch, "SELECT ACT", 100f, GameConfig.VIRTUAL_HEIGHT - 50f)
        game.batch.end()

        // Back button
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        game.shapeRenderer.color = Color(0.4f, 0.2f, 0.2f, 0.8f)
        game.shapeRenderer.rect(backButton.x, backButton.y, backButton.width, backButton.height)
        game.shapeRenderer.end()

        game.batch.begin()
        game.assetManager.uiFont.color = Color.WHITE
        game.assetManager.uiFont.draw(game.batch, "< BACK", backButton.x + 30f, backButton.y + 40f)
        game.batch.end()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
    override fun dispose() {}
}

data class ActData(
    val number: Int,
    val name: String,
    val bossName: String,
    val bossDescription: String,
    val color: Color,
    val recommendedLevel: Int
)

data class ActButton(
    val actData: ActData,
    val bounds: Rectangle
)
