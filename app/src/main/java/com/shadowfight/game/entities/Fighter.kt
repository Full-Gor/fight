package com.shadowfight.game.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.shadowfight.game.combat.*
import com.shadowfight.game.utils.GameConfig

/**
 * Base class for all fighters (player and enemies)
 * Handles physics, animations, combat states, and rendering
 */
open class Fighter(
    var position: Vector2,
    var facingRight: Boolean = true,
    val isPlayer: Boolean = true
) {
    // Physical properties
    var velocity: Vector2 = Vector2()
    var width: Float = 100f
    var height: Float = 200f
    var isGrounded: Boolean = true

    // Combat stats
    var maxHealth: Float = GameConfig.PLAYER_MAX_HEALTH
    var health: Float = maxHealth
    var maxEnergy: Float = GameConfig.PLAYER_MAX_ENERGY
    var energy: Float = maxEnergy

    // Equipment bonuses
    var attackBonus: Float = 0f
    var defenseBonus: Float = 0f
    var weaponDamageMultiplier: Float = 1f

    // State
    var state: FighterState = FighterState.IDLE
    var previousState: FighterState = FighterState.IDLE

    // Combat
    var currentAttack: Attack? = null
    var attackFrameCounter: Int = 0
    var isBlocking: Boolean = false
    var isHitStunned: Boolean = false
    var hitStunTimer: Float = 0f
    var invincibilityTimer: Float = 0f

    // Combo system
    var comboCount: Int = 0
    var comboTimer: Float = 0f
    var lastAttackTime: Long = 0

    // Animation
    var stateTime: Float = 0f
    var animationFrame: Int = 0

    // Hitboxes
    val bodyHitbox: Rectangle = Rectangle()
    val attackHitbox: Rectangle = Rectangle()

    // Visual
    var silhouetteTexture: Texture? = null
    var tintColor: Color = if (isPlayer) Color(0.1f, 0.1f, 0.2f, 1f) else Color(0.2f, 0.1f, 0.1f, 1f)

    // Equipment
    var equippedWeapon: Weapon = Weapon.FISTS
    var equippedArmor: Armor = Armor.BASIC
    var equippedHelm: Helm = Helm.NONE
    var equippedRanged: RangedWeapon = RangedWeapon.NONE
    var equippedMagic: Magic = Magic.NONE

    init {
        updateHitboxes()
    }

    open fun update(deltaTime: Float) {
        stateTime += deltaTime

        // Update physics
        if (!isGrounded) {
            velocity.y += GameConfig.GRAVITY * deltaTime * 60f
        }

        position.add(velocity.x * deltaTime * 60f, velocity.y * deltaTime * 60f)

        // Ground check
        if (position.y <= 150f) {
            position.y = 150f
            velocity.y = 0f
            isGrounded = true
            if (state == FighterState.JUMPING || state == FighterState.FALLING) {
                state = FighterState.IDLE
            }
        } else {
            isGrounded = false
        }

        // Update hit stun
        if (isHitStunned) {
            hitStunTimer -= deltaTime
            if (hitStunTimer <= 0) {
                isHitStunned = false
                state = FighterState.IDLE
            }
        }

        // Update invincibility
        if (invincibilityTimer > 0) {
            invincibilityTimer -= deltaTime
        }

        // Update attack
        if (currentAttack != null) {
            attackFrameCounter++
            updateAttackState()
        }

        // Update combo timer
        if (comboTimer > 0) {
            comboTimer -= deltaTime
            if (comboTimer <= 0) {
                comboCount = 0
            }
        }

        // Energy regeneration
        if (energy < maxEnergy && state == FighterState.IDLE) {
            energy = minOf(maxEnergy, energy + 10f * deltaTime)
        }

        updateHitboxes()
    }

    private fun updateAttackState() {
        val attack = currentAttack ?: return

        when {
            attackFrameCounter < attack.startupFrames -> {
                // Startup phase - can be interrupted
            }
            attackFrameCounter < attack.startupFrames + attack.activeFrames -> {
                // Active phase - hitbox is active
                state = FighterState.ATTACKING
            }
            attackFrameCounter < attack.totalFrames -> {
                // Recovery phase
                state = FighterState.RECOVERING
            }
            else -> {
                // Attack finished
                currentAttack = null
                attackFrameCounter = 0
                state = FighterState.IDLE
            }
        }
    }

    fun updateHitboxes() {
        // Body hitbox
        bodyHitbox.set(
            position.x - width / 2,
            position.y,
            width,
            height
        )

        // Attack hitbox (only active during attack)
        if (currentAttack != null && state == FighterState.ATTACKING) {
            val attack = currentAttack!!
            val attackX = if (facingRight) {
                position.x + width / 2
            } else {
                position.x - width / 2 - attack.range
            }
            attackHitbox.set(
                attackX,
                position.y + height * 0.3f,
                attack.range,
                attack.hitboxHeight
            )
        } else {
            attackHitbox.set(0f, 0f, 0f, 0f)
        }
    }

    fun startAttack(attack: Attack): Boolean {
        if (currentAttack != null || isHitStunned || isBlocking) return false

        // Check energy cost
        if (energy < attack.energyCost) return false

        currentAttack = attack
        attackFrameCounter = 0
        energy -= attack.energyCost
        state = FighterState.ATTACKING

        // Update combo
        val now = System.currentTimeMillis()
        if (now - lastAttackTime < GameConfig.COMBO_WINDOW_MS) {
            comboCount++
        } else {
            comboCount = 1
        }
        lastAttackTime = now
        comboTimer = GameConfig.COMBO_WINDOW_MS / 1000f

        return true
    }

    fun takeDamage(damage: Float, knockback: Vector2) {
        if (invincibilityTimer > 0) return

        var finalDamage = damage

        // Apply defense
        finalDamage -= defenseBonus

        // Apply blocking reduction
        if (isBlocking) {
            finalDamage *= (1f - GameConfig.BLOCK_DAMAGE_REDUCTION)
            // Small knockback when blocking
            velocity.x = knockback.x * 0.3f
        } else {
            // Full knockback and hit stun
            velocity.set(knockback)
            isHitStunned = true
            hitStunTimer = GameConfig.HIT_STUN_DURATION
            state = FighterState.HIT_STUN

            // Cancel current attack
            currentAttack = null
            attackFrameCounter = 0
        }

        health = maxOf(0f, health - finalDamage)

        // Brief invincibility
        invincibilityTimer = 0.1f
    }

    fun startBlocking() {
        if (currentAttack == null && !isHitStunned && isGrounded) {
            isBlocking = true
            state = FighterState.BLOCKING
        }
    }

    fun stopBlocking() {
        isBlocking = false
        if (state == FighterState.BLOCKING) {
            state = FighterState.IDLE
        }
    }

    fun jump() {
        if (isGrounded && !isHitStunned && currentAttack == null) {
            velocity.y = GameConfig.PLAYER_JUMP_FORCE
            isGrounded = false
            state = FighterState.JUMPING
        }
    }

    fun moveLeft() {
        if (!isHitStunned && currentAttack == null) {
            velocity.x = -GameConfig.PLAYER_WALK_SPEED
            facingRight = false
            if (isGrounded) state = FighterState.WALKING
        }
    }

    fun moveRight() {
        if (!isHitStunned && currentAttack == null) {
            velocity.x = GameConfig.PLAYER_WALK_SPEED
            facingRight = true
            if (isGrounded) state = FighterState.WALKING
        }
    }

    fun stopMoving() {
        velocity.x = 0f
        if (state == FighterState.WALKING) {
            state = FighterState.IDLE
        }
    }

    fun crouch() {
        if (isGrounded && !isHitStunned && currentAttack == null) {
            state = FighterState.CROUCHING
            height = 120f // Reduced height when crouching
        }
    }

    fun standUp() {
        if (state == FighterState.CROUCHING) {
            state = FighterState.IDLE
            height = 200f
        }
    }

    fun isAlive(): Boolean = health > 0

    fun reset() {
        health = maxHealth
        energy = maxEnergy
        state = FighterState.IDLE
        velocity.set(0f, 0f)
        isHitStunned = false
        isBlocking = false
        currentAttack = null
        comboCount = 0
    }

    fun render(batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        // Render silhouette texture if available
        silhouetteTexture?.let { texture ->
            val drawX = position.x - width / 2
            val drawY = position.y
            val scaleX = if (facingRight) 1f else -1f

            batch.color = tintColor
            batch.draw(
                texture,
                if (facingRight) drawX else drawX + width,
                drawY,
                width * scaleX,
                height
            )
            batch.color = Color.WHITE
        }

        // Debug hitboxes (can be toggled)
        if (false) { // Set to true for debug
            batch.end()
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)

            // Body hitbox
            shapeRenderer.color = Color.GREEN
            shapeRenderer.rect(bodyHitbox.x, bodyHitbox.y, bodyHitbox.width, bodyHitbox.height)

            // Attack hitbox
            if (attackHitbox.width > 0) {
                shapeRenderer.color = Color.RED
                shapeRenderer.rect(attackHitbox.x, attackHitbox.y, attackHitbox.width, attackHitbox.height)
            }

            shapeRenderer.end()
            batch.begin()
        }
    }
}

enum class FighterState {
    IDLE,
    WALKING,
    RUNNING,
    JUMPING,
    FALLING,
    CROUCHING,
    ATTACKING,
    RECOVERING,
    BLOCKING,
    HIT_STUN,
    KNOCKED_DOWN,
    GETTING_UP,
    VICTORY,
    DEFEAT
}
