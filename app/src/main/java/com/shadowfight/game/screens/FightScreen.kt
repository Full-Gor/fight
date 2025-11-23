package com.shadowfight.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.FitViewport
import com.shadowfight.game.core.EnemyType
import com.shadowfight.game.core.ShadowFightGame
import com.shadowfight.game.entities.Fighter
import com.shadowfight.game.entities.FighterState
import com.shadowfight.game.combat.*
import com.shadowfight.game.ai.FighterAI
import com.shadowfight.game.input.TouchControls
import com.shadowfight.game.utils.GameConfig

/**
 * Main fight screen where combat takes place
 */
class FightScreen(
    private val game: ShadowFightGame,
    private val enemyType: EnemyType,
    private val actNumber: Int
) : Screen {

    private val camera = OrthographicCamera()
    private val viewport = FitViewport(GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT, camera)

    // Fighters
    private lateinit var player: Fighter
    private lateinit var enemy: Fighter
    private lateinit var enemyAI: FighterAI

    // Controls
    private lateinit var touchControls: TouchControls

    // Fight state
    private var roundNumber = 1
    private var playerWins = 0
    private var enemyWins = 0
    private var roundTime = GameConfig.ROUND_TIME.toFloat()
    private var fightState = FightState.INTRO
    private var stateTimer = 0f
    private var isPaused = false

    // Visual effects
    private val damageNumbers = mutableListOf<DamageNumber>()
    private var hitEffectTimer = 0f
    private var screenShakeIntensity = 0f

    override fun show() {
        camera.position.set(GameConfig.VIRTUAL_WIDTH / 2, GameConfig.VIRTUAL_HEIGHT / 2, 0f)
        initFighters()
        touchControls = TouchControls(camera)
    }

    private fun initFighters() {
        // Create player
        player = Fighter(
            position = Vector2(300f, 150f),
            facingRight = true,
            isPlayer = true
        ).apply {
            silhouetteTexture = game.assetManager.playerSilhouette
            equippedWeapon = Weapon.getByName(game.playerData.equippedWeapon)
            equippedArmor = Armor.getByName(game.playerData.equippedArmor)

            // Apply equipment bonuses
            attackBonus = equippedWeapon.damageMultiplier * 10f
            defenseBonus = equippedArmor.defenseBonus
            maxHealth = GameConfig.PLAYER_MAX_HEALTH + equippedArmor.healthBonus
            health = maxHealth
        }

        // Create enemy based on type
        val enemyLevel = when (enemyType) {
            EnemyType.REGULAR -> game.playerData.level
            EnemyType.TOURNAMENT -> game.playerData.level + 1
            EnemyType.BOSS -> game.playerData.level + 3
            EnemyType.DEMON -> game.playerData.level + 5
            EnemyType.SURVIVAL -> game.playerData.level
        }

        enemy = Fighter(
            position = Vector2(GameConfig.VIRTUAL_WIDTH - 300f, 150f),
            facingRight = false,
            isPlayer = false
        ).apply {
            silhouetteTexture = game.assetManager.enemySilhouette
            tintColor = when (enemyType) {
                EnemyType.BOSS -> Color(0.3f, 0.05f, 0.05f, 1f)
                EnemyType.DEMON -> Color(0.4f, 0.0f, 0.1f, 1f)
                else -> Color(0.15f, 0.05f, 0.1f, 1f)
            }

            // Scale enemy stats based on level
            val statMultiplier = 1f + (enemyLevel - 1) * 0.15f
            maxHealth = GameConfig.PLAYER_MAX_HEALTH * statMultiplier
            health = maxHealth
            attackBonus = 5f * statMultiplier
            defenseBonus = 3f * statMultiplier
        }

        // Create AI controller
        val difficulty = when (enemyType) {
            EnemyType.REGULAR -> 0.4f
            EnemyType.TOURNAMENT -> 0.6f
            EnemyType.BOSS -> 0.8f
            EnemyType.DEMON -> 0.95f
            EnemyType.SURVIVAL -> 0.5f
        }
        enemyAI = FighterAI(enemy, player, difficulty)

        fightState = FightState.INTRO
        stateTimer = 2f
    }

    override fun render(delta: Float) {
        if (isPaused) {
            renderPauseMenu()
            return
        }

        update(delta)
        draw()
    }

    private fun update(delta: Float) {
        // Update state timer
        if (stateTimer > 0) {
            stateTimer -= delta
        }

        when (fightState) {
            FightState.INTRO -> {
                if (stateTimer <= 0) {
                    fightState = FightState.FIGHTING
                }
            }
            FightState.FIGHTING -> {
                updateFighting(delta)
            }
            FightState.ROUND_END -> {
                if (stateTimer <= 0) {
                    startNextRound()
                }
            }
            FightState.MATCH_END -> {
                handleMatchEnd()
            }
        }

        // Update visual effects
        updateEffects(delta)

        // Handle pause
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            isPaused = !isPaused
        }
    }

    private fun updateFighting(delta: Float) {
        // Update round timer
        roundTime -= delta
        if (roundTime <= 0) {
            endRound(if (player.health > enemy.health) player else enemy)
            return
        }

        // Handle player input
        handlePlayerInput(delta)

        // Update AI
        enemyAI.update(delta)

        // Update fighters
        player.update(delta)
        enemy.update(delta)

        // Keep fighters facing each other
        player.facingRight = player.position.x < enemy.position.x
        enemy.facingRight = enemy.position.x < player.position.x

        // Keep fighters in bounds
        keepInBounds(player)
        keepInBounds(enemy)

        // Check for hits
        checkCombatCollisions()

        // Check for round end
        if (!player.isAlive()) {
            endRound(enemy)
        } else if (!enemy.isAlive()) {
            endRound(player)
        }
    }

    private fun handlePlayerInput(delta: Float) {
        // Get touch/virtual joystick input
        val touchInput = touchControls.update()

        // Keyboard controls (for desktop)
        var moveX = 0f
        var moveY = 0f

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            moveX = -1f
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            moveX = 1f
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            player.jump()
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            player.crouch()
        } else {
            player.standUp()
        }

        // Apply movement from touch or keyboard
        moveX += touchInput.moveDirection.x
        if (touchInput.jump) player.jump()
        if (touchInput.crouch) player.crouch() else if (!Gdx.input.isKeyPressed(Input.Keys.DOWN)) player.standUp()

        when {
            moveX < -0.3f -> player.moveLeft()
            moveX > 0.3f -> player.moveRight()
            else -> player.stopMoving()
        }

        // Attack inputs
        if (Gdx.input.isKeyJustPressed(Input.Keys.J) || touchInput.punch) {
            performPlayerAttack(AttackType.PUNCH)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.K) || touchInput.kick) {
            performPlayerAttack(AttackType.KICK)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.L) || touchInput.block) {
            player.startBlocking()
        } else {
            player.stopBlocking()
        }
    }

    private fun performPlayerAttack(type: AttackType) {
        val attack = when {
            !player.isGrounded && type == AttackType.PUNCH -> Attacks.JUMP_PUNCH
            !player.isGrounded && type == AttackType.KICK -> Attacks.JUMP_KICK
            player.state == FighterState.CROUCHING && type == AttackType.KICK -> Attacks.SWEEP
            type == AttackType.PUNCH -> {
                when (player.comboCount % 3) {
                    0 -> Attacks.JAB
                    1 -> Attacks.STRAIGHT
                    else -> Attacks.HOOK
                }
            }
            else -> {
                when (player.comboCount % 3) {
                    0 -> Attacks.LOW_KICK
                    1 -> Attacks.MIDDLE_KICK
                    else -> Attacks.HIGH_KICK
                }
            }
        }

        // Apply weapon damage multiplier
        val weaponizedAttack = if (player.equippedWeapon != Weapon.FISTS && type == AttackType.PUNCH) {
            attack.copy(
                damage = attack.damage * player.equippedWeapon.damageMultiplier,
                range = attack.range + player.equippedWeapon.rangeBonus
            )
        } else {
            attack
        }

        player.startAttack(weaponizedAttack)
    }

    private fun checkCombatCollisions() {
        // Player attacking enemy
        if (player.state == FighterState.ATTACKING && player.attackHitbox.overlaps(enemy.bodyHitbox)) {
            val attack = player.currentAttack
            if (attack != null && player.attackFrameCounter == attack.startupFrames + 1) {
                val damage = attack.damage + player.attackBonus
                val knockbackDir = if (player.facingRight) 1f else -1f
                val knockback = Vector2(attack.knockbackX * knockbackDir, attack.knockbackY)

                enemy.takeDamage(damage, knockback)
                addDamageNumber(enemy.position.x, enemy.position.y + enemy.height, damage)
                triggerHitEffect()

                // Update combo counter display
                if (!enemy.isBlocking) {
                    player.comboCount++
                }
            }
        }

        // Enemy attacking player
        if (enemy.state == FighterState.ATTACKING && enemy.attackHitbox.overlaps(player.bodyHitbox)) {
            val attack = enemy.currentAttack
            if (attack != null && enemy.attackFrameCounter == attack.startupFrames + 1) {
                val damage = attack.damage + enemy.attackBonus
                val knockbackDir = if (enemy.facingRight) 1f else -1f
                val knockback = Vector2(attack.knockbackX * knockbackDir, attack.knockbackY)

                player.takeDamage(damage, knockback)
                addDamageNumber(player.position.x, player.position.y + player.height, damage)
                triggerHitEffect()
            }
        }
    }

    private fun keepInBounds(fighter: Fighter) {
        val minX = 50f + fighter.width / 2
        val maxX = GameConfig.VIRTUAL_WIDTH - 50f - fighter.width / 2

        if (fighter.position.x < minX) {
            fighter.position.x = minX
            fighter.velocity.x = 0f
        }
        if (fighter.position.x > maxX) {
            fighter.position.x = maxX
            fighter.velocity.x = 0f
        }
    }

    private fun endRound(winner: Fighter) {
        if (winner == player) {
            playerWins++
            player.state = FighterState.VICTORY
        } else {
            enemyWins++
            enemy.state = FighterState.VICTORY
        }

        fightState = if (playerWins >= GameConfig.ROUNDS_TO_WIN || enemyWins >= GameConfig.ROUNDS_TO_WIN) {
            stateTimer = 3f
            FightState.MATCH_END
        } else {
            stateTimer = 2f
            FightState.ROUND_END
        }
    }

    private fun startNextRound() {
        roundNumber++
        roundTime = GameConfig.ROUND_TIME.toFloat()
        player.reset()
        enemy.reset()
        player.position.set(300f, 150f)
        enemy.position.set(GameConfig.VIRTUAL_WIDTH - 300f, 150f)
        fightState = FightState.INTRO
        stateTimer = 1.5f
    }

    private fun handleMatchEnd() {
        if (stateTimer <= 0) {
            // Award rewards for winning
            if (playerWins > enemyWins) {
                val coinsEarned = when (enemyType) {
                    EnemyType.REGULAR -> 50
                    EnemyType.TOURNAMENT -> 150
                    EnemyType.BOSS -> 500
                    EnemyType.DEMON -> 1000
                    EnemyType.SURVIVAL -> 75
                }
                val xpEarned = when (enemyType) {
                    EnemyType.REGULAR -> 20
                    EnemyType.TOURNAMENT -> 50
                    EnemyType.BOSS -> 200
                    EnemyType.DEMON -> 500
                    EnemyType.SURVIVAL -> 30
                }
                game.playerData.coins += coinsEarned
                game.playerData.experience += xpEarned

                // Level up check
                val xpNeeded = game.playerData.level * 100
                if (game.playerData.experience >= xpNeeded) {
                    game.playerData.level++
                    game.playerData.experience -= xpNeeded
                }
            }
            game.goToMainMenu()
        }
    }

    private fun addDamageNumber(x: Float, y: Float, damage: Float) {
        damageNumbers.add(DamageNumber(x, y, damage.toInt()))
    }

    private fun triggerHitEffect() {
        screenShakeIntensity = 8f
        hitEffectTimer = 0.1f
    }

    private fun updateEffects(delta: Float) {
        // Update damage numbers
        damageNumbers.forEach { it.update(delta) }
        damageNumbers.removeAll { it.lifetime <= 0 }

        // Update screen shake
        if (screenShakeIntensity > 0) {
            screenShakeIntensity -= delta * 50f
            if (screenShakeIntensity < 0) screenShakeIntensity = 0f
        }

        // Update hit effect
        if (hitEffectTimer > 0) {
            hitEffectTimer -= delta
        }
    }

    private fun draw() {
        // Apply screen shake
        val shakeX = if (screenShakeIntensity > 0) (Math.random() * screenShakeIntensity - screenShakeIntensity / 2).toFloat() else 0f
        val shakeY = if (screenShakeIntensity > 0) (Math.random() * screenShakeIntensity - screenShakeIntensity / 2).toFloat() else 0f

        camera.position.set(
            GameConfig.VIRTUAL_WIDTH / 2 + shakeX,
            GameConfig.VIRTUAL_HEIGHT / 2 + shakeY,
            0f
        )
        camera.update()

        // Clear with hit flash effect
        val flashIntensity = if (hitEffectTimer > 0) 0.3f else 0f
        Gdx.gl.glClearColor(flashIntensity, flashIntensity * 0.5f, flashIntensity * 0.5f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        game.batch.projectionMatrix = camera.combined
        game.shapeRenderer.projectionMatrix = camera.combined

        // Draw background
        game.batch.begin()
        game.batch.draw(game.assetManager.backgroundTexture, 0f, 0f, GameConfig.VIRTUAL_WIDTH, GameConfig.VIRTUAL_HEIGHT)
        game.batch.end()

        // Draw fighters
        game.batch.begin()
        player.render(game.batch, game.shapeRenderer)
        enemy.render(game.batch, game.shapeRenderer)
        game.batch.end()

        // Draw UI
        drawUI()

        // Draw touch controls
        touchControls.render(game.shapeRenderer, game.batch)
    }

    private fun drawUI() {
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Health bars background
        game.shapeRenderer.color = Color(0.2f, 0.2f, 0.2f, 0.8f)
        game.shapeRenderer.rect(50f, GameConfig.VIRTUAL_HEIGHT - 80f, 500f, 40f)
        game.shapeRenderer.rect(GameConfig.VIRTUAL_WIDTH - 550f, GameConfig.VIRTUAL_HEIGHT - 80f, 500f, 40f)

        // Player health bar
        val playerHealthPercent = player.health / player.maxHealth
        game.shapeRenderer.color = Color.GREEN.cpy().lerp(Color.RED, 1f - playerHealthPercent)
        game.shapeRenderer.rect(50f, GameConfig.VIRTUAL_HEIGHT - 80f, 500f * playerHealthPercent, 40f)

        // Enemy health bar
        val enemyHealthPercent = enemy.health / enemy.maxHealth
        game.shapeRenderer.color = Color.GREEN.cpy().lerp(Color.RED, 1f - enemyHealthPercent)
        val enemyBarWidth = 500f * enemyHealthPercent
        game.shapeRenderer.rect(GameConfig.VIRTUAL_WIDTH - 50f - enemyBarWidth, GameConfig.VIRTUAL_HEIGHT - 80f, enemyBarWidth, 40f)

        // Energy bars
        game.shapeRenderer.color = Color(0.1f, 0.1f, 0.1f, 0.8f)
        game.shapeRenderer.rect(50f, GameConfig.VIRTUAL_HEIGHT - 100f, 300f, 15f)

        game.shapeRenderer.color = Color(0.2f, 0.5f, 1f, 1f)
        game.shapeRenderer.rect(50f, GameConfig.VIRTUAL_HEIGHT - 100f, 300f * (player.energy / player.maxEnergy), 15f)

        game.shapeRenderer.end()

        // Draw text UI
        game.batch.begin()

        // Timer
        game.assetManager.titleFont.color = Color.WHITE
        val timerText = roundTime.toInt().toString()
        val timerLayout = GlyphLayout(game.assetManager.titleFont, timerText)
        game.assetManager.titleFont.draw(
            game.batch,
            timerText,
            (GameConfig.VIRTUAL_WIDTH - timerLayout.width) / 2,
            GameConfig.VIRTUAL_HEIGHT - 30f
        )

        // Round indicator
        game.assetManager.uiFont.color = Color.YELLOW
        val roundText = "Round $roundNumber"
        val roundLayout = GlyphLayout(game.assetManager.uiFont, roundText)
        game.assetManager.uiFont.draw(
            game.batch,
            roundText,
            (GameConfig.VIRTUAL_WIDTH - roundLayout.width) / 2,
            GameConfig.VIRTUAL_HEIGHT - 110f
        )

        // Win indicators
        game.assetManager.uiFont.color = Color.CYAN
        game.assetManager.uiFont.draw(game.batch, "Wins: $playerWins", 50f, GameConfig.VIRTUAL_HEIGHT - 120f)
        game.assetManager.uiFont.draw(game.batch, "Wins: $enemyWins", GameConfig.VIRTUAL_WIDTH - 150f, GameConfig.VIRTUAL_HEIGHT - 120f)

        // Combo display
        if (player.comboCount > 1) {
            game.assetManager.titleFont.color = Color.ORANGE
            val comboText = "${player.comboCount} HITS!"
            game.assetManager.titleFont.draw(game.batch, comboText, 50f, 300f)
        }

        // Fight state messages
        when (fightState) {
            FightState.INTRO -> {
                game.assetManager.titleFont.color = Color.WHITE
                val text = if (stateTimer > 1f) "ROUND $roundNumber" else "FIGHT!"
                val layout = GlyphLayout(game.assetManager.titleFont, text)
                game.assetManager.titleFont.draw(
                    game.batch,
                    text,
                    (GameConfig.VIRTUAL_WIDTH - layout.width) / 2,
                    GameConfig.VIRTUAL_HEIGHT / 2 + 100f
                )
            }
            FightState.ROUND_END, FightState.MATCH_END -> {
                val text = if (playerWins > enemyWins) "VICTORY!" else "DEFEAT"
                game.assetManager.titleFont.color = if (playerWins > enemyWins) Color.GOLD else Color.RED
                val layout = GlyphLayout(game.assetManager.titleFont, text)
                game.assetManager.titleFont.draw(
                    game.batch,
                    text,
                    (GameConfig.VIRTUAL_WIDTH - layout.width) / 2,
                    GameConfig.VIRTUAL_HEIGHT / 2 + 100f
                )
            }
            else -> {}
        }

        // Damage numbers
        damageNumbers.forEach { dmgNum ->
            game.assetManager.damageFont.color = Color(1f, 0.3f, 0.3f, dmgNum.lifetime / 1.5f)
            game.assetManager.damageFont.draw(game.batch, dmgNum.value.toString(), dmgNum.x, dmgNum.y)
        }

        game.batch.end()
    }

    private fun renderPauseMenu() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 0.7f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        game.batch.begin()
        game.assetManager.titleFont.color = Color.WHITE
        val pauseText = "PAUSED"
        val layout = GlyphLayout(game.assetManager.titleFont, pauseText)
        game.assetManager.titleFont.draw(
            game.batch,
            pauseText,
            (GameConfig.VIRTUAL_WIDTH - layout.width) / 2,
            GameConfig.VIRTUAL_HEIGHT / 2 + 50f
        )

        game.assetManager.uiFont.color = Color.GRAY
        game.assetManager.uiFont.draw(
            game.batch,
            "Press ESC or P to resume",
            GameConfig.VIRTUAL_WIDTH / 2 - 150f,
            GameConfig.VIRTUAL_HEIGHT / 2 - 50f
        )
        game.batch.end()

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            isPaused = false
        }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        touchControls.resize(width, height)
    }

    override fun pause() {
        isPaused = true
    }

    override fun resume() {}
    override fun hide() {}
    override fun dispose() {}
}

enum class FightState {
    INTRO,
    FIGHTING,
    ROUND_END,
    MATCH_END
}

data class DamageNumber(
    var x: Float,
    var y: Float,
    val value: Int,
    var lifetime: Float = 1.5f
) {
    fun update(delta: Float) {
        y += delta * 100f
        lifetime -= delta
    }
}
