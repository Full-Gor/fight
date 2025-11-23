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
import com.shadowfight.game.combat.*
import com.shadowfight.game.utils.GameConfig

/**
 * Equipment/Shop screen for managing player gear
 */
class EquipmentScreen(private val game: ShadowFightGame) : Screen {

    private val camera = OrthographicCamera()
    private val viewport = FitViewport(GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT, camera)
    private val touchPoint = Vector3()

    // Categories
    private var selectedCategory = EquipmentCategory.WEAPONS
    private val categoryButtons = mutableListOf<CategoryButton>()

    // Items
    private var selectedItemIndex = 0
    private var scrollOffset = 0
    private val maxVisibleItems = 5
    private val itemButtons = mutableListOf<Rectangle>()

    // Back button
    private val backButton = Rectangle(50f, 50f, 150f, 60f)

    init {
        val buttonWidth = 180f
        val buttonHeight = 50f
        val startX = 100f
        val y = GameConfig.VIRTUAL_HEIGHT - 150f

        EquipmentCategory.values().forEachIndexed { index, category ->
            categoryButtons.add(
                CategoryButton(
                    category,
                    Rectangle(startX + index * (buttonWidth + 20), y, buttonWidth, buttonHeight)
                )
            )
        }

        // Create item button positions
        for (i in 0 until maxVisibleItems) {
            itemButtons.add(Rectangle(100f, 600f - i * 100f, 600f, 80f))
        }
    }

    override fun show() {
        camera.position.set(GameConfig.VIRTUAL_WIDTH / 2, GameConfig.VIRTUAL_HEIGHT / 2, 0f)
    }

    override fun render(delta: Float) {
        handleInput()

        Gdx.gl.glClearColor(0.05f, 0.03f, 0.08f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()

        drawBackground()
        drawCategories()
        drawItems()
        drawPlayerPreview()
        drawUI()
    }

    private fun handleInput() {
        // Keyboard navigation
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedItemIndex = maxOf(0, selectedItemIndex - 1)
            if (selectedItemIndex < scrollOffset) scrollOffset = selectedItemIndex
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            val items = getCurrentItems()
            selectedItemIndex = minOf(items.size - 1, selectedItemIndex + 1)
            if (selectedItemIndex >= scrollOffset + maxVisibleItems) {
                scrollOffset = selectedItemIndex - maxVisibleItems + 1
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            val categoryIndex = selectedCategory.ordinal
            selectedCategory = EquipmentCategory.values()[(categoryIndex - 1 + EquipmentCategory.values().size) % EquipmentCategory.values().size]
            selectedItemIndex = 0
            scrollOffset = 0
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            val categoryIndex = selectedCategory.ordinal
            selectedCategory = EquipmentCategory.values()[(categoryIndex + 1) % EquipmentCategory.values().size]
            selectedItemIndex = 0
            scrollOffset = 0
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            equipSelectedItem()
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.goToMainMenu()
        }

        // Touch input
        if (Gdx.input.justTouched()) {
            touchPoint.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            camera.unproject(touchPoint)

            // Check category buttons
            categoryButtons.forEach { button ->
                if (button.bounds.contains(touchPoint.x, touchPoint.y)) {
                    selectedCategory = button.category
                    selectedItemIndex = 0
                    scrollOffset = 0
                }
            }

            // Check item buttons
            val items = getCurrentItems()
            itemButtons.forEachIndexed { index, bounds ->
                val itemIndex = scrollOffset + index
                if (bounds.contains(touchPoint.x, touchPoint.y) && itemIndex < items.size) {
                    if (selectedItemIndex == itemIndex) {
                        equipSelectedItem()
                    } else {
                        selectedItemIndex = itemIndex
                    }
                }
            }

            // Check back button
            if (backButton.contains(touchPoint.x, touchPoint.y)) {
                game.goToMainMenu()
            }
        }
    }

    private fun getCurrentItems(): List<EquipmentItem> {
        return when (selectedCategory) {
            EquipmentCategory.WEAPONS -> Weapon.values().map { EquipmentItem(it.displayName, it.name, it.price, it.requiredLevel, "${it.damageMultiplier}x damage, +${it.rangeBonus.toInt()} range") }
            EquipmentCategory.ARMOR -> Armor.values().map { EquipmentItem(it.displayName, it.name, it.price, it.requiredLevel, "+${it.defenseBonus.toInt()} defense, +${it.healthBonus.toInt()} HP") }
            EquipmentCategory.HELM -> Helm.values().map { EquipmentItem(it.displayName, it.name, it.price, it.requiredLevel, "+${it.defenseBonus.toInt()} defense ${it.specialEffect}") }
            EquipmentCategory.RANGED -> RangedWeapon.values().map { EquipmentItem(it.displayName, it.name, it.price, it.requiredLevel, "${it.damage.toInt()} damage, ${it.range.toInt()} range") }
            EquipmentCategory.MAGIC -> Magic.values().map { EquipmentItem(it.displayName, it.name, it.price, it.requiredLevel, "${it.damage.toInt()} damage, ${it.effect}") }
        }
    }

    private fun equipSelectedItem() {
        val items = getCurrentItems()
        if (selectedItemIndex >= items.size) return

        val item = items[selectedItemIndex]

        // Check if can afford and meets level requirement
        if (item.price > game.playerData.coins && !isOwned(item)) {
            return // Can't afford
        }
        if (item.requiredLevel > game.playerData.level) {
            return // Level too low
        }

        // Buy if not owned
        if (!isOwned(item) && item.price > 0) {
            game.playerData.coins -= item.price
        }

        // Equip item
        when (selectedCategory) {
            EquipmentCategory.WEAPONS -> {
                game.playerData.equippedWeapon = item.id
                if (!game.playerData.unlockedWeapons.contains(item.id)) {
                    game.playerData.unlockedWeapons.add(item.id)
                }
            }
            EquipmentCategory.ARMOR -> {
                game.playerData.equippedArmor = item.id
                if (!game.playerData.unlockedArmors.contains(item.id)) {
                    game.playerData.unlockedArmors.add(item.id)
                }
            }
            EquipmentCategory.HELM -> game.playerData.equippedHelm = item.id
            EquipmentCategory.RANGED -> game.playerData.equippedRanged = item.id
            EquipmentCategory.MAGIC -> game.playerData.equippedMagic = item.id
        }
    }

    private fun isOwned(item: EquipmentItem): Boolean {
        return when (selectedCategory) {
            EquipmentCategory.WEAPONS -> game.playerData.unlockedWeapons.contains(item.id)
            EquipmentCategory.ARMOR -> game.playerData.unlockedArmors.contains(item.id)
            else -> item.price == 0 // Free items are always "owned"
        }
    }

    private fun isEquipped(item: EquipmentItem): Boolean {
        return when (selectedCategory) {
            EquipmentCategory.WEAPONS -> game.playerData.equippedWeapon == item.id
            EquipmentCategory.ARMOR -> game.playerData.equippedArmor == item.id
            EquipmentCategory.HELM -> game.playerData.equippedHelm == item.id
            EquipmentCategory.RANGED -> game.playerData.equippedRanged == item.id
            EquipmentCategory.MAGIC -> game.playerData.equippedMagic == item.id
        }
    }

    private fun drawBackground() {
        game.batch.projectionMatrix = camera.combined
        game.batch.begin()
        game.batch.color = Color(0.3f, 0.3f, 0.3f, 1f)
        game.batch.draw(game.assetManager.backgroundTexture, 0f, 0f, GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT)
        game.batch.color = Color.WHITE
        game.batch.end()
    }

    private fun drawCategories() {
        game.shapeRenderer.projectionMatrix = camera.combined
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        categoryButtons.forEach { button ->
            val isSelected = button.category == selectedCategory
            game.shapeRenderer.color = if (isSelected) Color(0.4f, 0.2f, 0.6f, 0.9f) else Color(0.2f, 0.1f, 0.3f, 0.8f)
            game.shapeRenderer.rect(button.bounds.x, button.bounds.y, button.bounds.width, button.bounds.height)
        }

        game.shapeRenderer.end()

        game.batch.begin()
        categoryButtons.forEach { button ->
            val isSelected = button.category == selectedCategory
            game.assetManager.uiFont.color = if (isSelected) Color.WHITE else Color.GRAY
            val layout = GlyphLayout(game.assetManager.uiFont, button.category.displayName)
            game.assetManager.uiFont.draw(
                game.batch,
                button.category.displayName,
                button.bounds.x + (button.bounds.width - layout.width) / 2,
                button.bounds.y + (button.bounds.height + layout.height) / 2
            )
        }
        game.batch.end()
    }

    private fun drawItems() {
        val items = getCurrentItems()

        game.shapeRenderer.projectionMatrix = camera.combined
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        itemButtons.forEachIndexed { index, bounds ->
            val itemIndex = scrollOffset + index
            if (itemIndex < items.size) {
                val item = items[itemIndex]
                val isSelected = itemIndex == selectedItemIndex
                val isEquippedItem = isEquipped(item)

                val bgColor = when {
                    isEquippedItem -> Color(0.2f, 0.5f, 0.2f, 0.8f)
                    isSelected -> Color(0.3f, 0.2f, 0.5f, 0.9f)
                    else -> Color(0.15f, 0.1f, 0.2f, 0.8f)
                }
                game.shapeRenderer.color = bgColor
                game.shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height)
            }
        }

        game.shapeRenderer.end()

        game.batch.begin()
        itemButtons.forEachIndexed { index, bounds ->
            val itemIndex = scrollOffset + index
            if (itemIndex < items.size) {
                val item = items[itemIndex]
                val isOwned = isOwned(item)
                val canAfford = game.playerData.coins >= item.price
                val meetsLevel = game.playerData.level >= item.requiredLevel

                // Item name
                game.assetManager.uiFont.color = if (meetsLevel) Color.WHITE else Color.GRAY
                game.assetManager.uiFont.draw(game.batch, item.name, bounds.x + 20f, bounds.y + 55f)

                // Stats
                game.assetManager.uiFont.color = Color.LIGHT_GRAY
                game.assetManager.uiFont.draw(game.batch, item.stats, bounds.x + 20f, bounds.y + 25f)

                // Price or status
                val statusText = when {
                    isEquipped(item) -> "EQUIPPED"
                    isOwned -> "OWNED"
                    !meetsLevel -> "Lv.${item.requiredLevel}"
                    else -> "${item.price} coins"
                }
                val statusColor = when {
                    isEquipped(item) -> Color.GREEN
                    isOwned -> Color.CYAN
                    !meetsLevel -> Color.RED
                    canAfford -> Color.GOLD
                    else -> Color.RED
                }
                game.assetManager.uiFont.color = statusColor
                game.assetManager.uiFont.draw(game.batch, statusText, bounds.x + bounds.width - 150f, bounds.y + 45f)
            }
        }
        game.batch.end()
    }

    private fun drawPlayerPreview() {
        // Draw character preview on the right side
        game.batch.begin()
        game.batch.draw(
            game.assetManager.playerSilhouette,
            GameConfig.VIRTUAL_WIDTH - 400f,
            200f,
            200f,
            400f
        )
        game.batch.end()

        // Draw equipped items text
        game.batch.begin()
        game.assetManager.uiFont.color = Color.WHITE
        game.assetManager.uiFont.draw(game.batch, "Equipped:", GameConfig.VIRTUAL_WIDTH - 380f, 650f)

        game.assetManager.uiFont.color = Color.CYAN
        game.assetManager.uiFont.draw(game.batch, "Weapon: ${game.playerData.equippedWeapon}", GameConfig.VIRTUAL_WIDTH - 380f, 620f)
        game.assetManager.uiFont.draw(game.batch, "Armor: ${game.playerData.equippedArmor}", GameConfig.VIRTUAL_WIDTH - 380f, 590f)
        game.assetManager.uiFont.draw(game.batch, "Helm: ${game.playerData.equippedHelm}", GameConfig.VIRTUAL_WIDTH - 380f, 560f)
        game.batch.end()
    }

    private fun drawUI() {
        // Title
        game.batch.begin()
        game.assetManager.titleFont.color = Color.WHITE
        game.assetManager.titleFont.draw(game.batch, "EQUIPMENT", 100f, GameConfig.VIRTUAL_HEIGHT - 50f)

        // Coins
        game.assetManager.uiFont.color = Color.GOLD
        game.assetManager.uiFont.draw(game.batch, "Coins: ${game.playerData.coins}", GameConfig.VIRTUAL_WIDTH - 300f, GameConfig.VIRTUAL_HEIGHT - 50f)

        // Level
        game.assetManager.uiFont.color = Color.CYAN
        game.assetManager.uiFont.draw(game.batch, "Level: ${game.playerData.level}", GameConfig.VIRTUAL_WIDTH - 300f, GameConfig.VIRTUAL_HEIGHT - 80f)
        game.batch.end()

        // Back button
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        game.shapeRenderer.color = Color(0.5f, 0.2f, 0.2f, 0.8f)
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

enum class EquipmentCategory(val displayName: String) {
    WEAPONS("Weapons"),
    ARMOR("Armor"),
    HELM("Helm"),
    RANGED("Ranged"),
    MAGIC("Magic")
}

data class CategoryButton(
    val category: EquipmentCategory,
    val bounds: Rectangle
)

data class EquipmentItem(
    val name: String,
    val id: String,
    val price: Int,
    val requiredLevel: Int,
    val stats: String
)
