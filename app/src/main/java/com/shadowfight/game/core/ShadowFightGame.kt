package com.shadowfight.game.core

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.shadowfight.game.screens.*
import com.shadowfight.game.assets.AssetManager
import com.shadowfight.game.utils.GameConfig

/**
 * Main game class for Shadow Fight 2 Clone
 * Manages screens, assets, and core game systems
 */
class ShadowFightGame : Game() {

    lateinit var batch: SpriteBatch
        private set
    lateinit var shapeRenderer: ShapeRenderer
        private set
    lateinit var assetManager: AssetManager
        private set

    // Game state
    var playerData: PlayerData = PlayerData()

    override fun create() {
        Gdx.app.log(TAG, "Initializing Shadow Fight 2 Clone...")

        batch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        assetManager = AssetManager()

        // Load initial assets
        assetManager.loadInitialAssets()

        // Start with splash screen
        setScreen(SplashScreen(this))
    }

    fun goToMainMenu() {
        setScreen(MainMenuScreen(this))
    }

    fun startFight(enemyType: EnemyType = EnemyType.REGULAR, actNumber: Int = 1) {
        setScreen(FightScreen(this, enemyType, actNumber))
    }

    fun goToEquipment() {
        setScreen(EquipmentScreen(this))
    }

    fun goToActSelection() {
        setScreen(ActSelectionScreen(this))
    }

    override fun render() {
        super.render()
    }

    override fun dispose() {
        Gdx.app.log(TAG, "Disposing game resources...")
        batch.dispose()
        shapeRenderer.dispose()
        assetManager.dispose()
        screen?.dispose()
    }

    companion object {
        const val TAG = "ShadowFight2"
    }
}

/**
 * Player progression data
 */
data class PlayerData(
    var level: Int = 1,
    var experience: Int = 0,
    var coins: Int = 1000,
    var gems: Int = 50,
    var currentAct: Int = 1,
    var equippedWeapon: String = "fists",
    var equippedArmor: String = "basic",
    var equippedHelm: String = "none",
    var equippedRanged: String = "none",
    var equippedMagic: String = "none",
    var unlockedWeapons: MutableList<String> = mutableListOf("fists", "wooden_sword"),
    var unlockedArmors: MutableList<String> = mutableListOf("basic"),
    var defeatedBosses: MutableList<String> = mutableListOf()
)

enum class EnemyType {
    REGULAR,
    TOURNAMENT,
    SURVIVAL,
    BOSS,
    DEMON
}
