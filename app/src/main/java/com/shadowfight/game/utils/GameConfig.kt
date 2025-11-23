package com.shadowfight.game.utils

/**
 * Game configuration constants
 */
object GameConfig {
    // Screen dimensions (virtual)
    const val VIRTUAL_WIDTH = 1920f
    const val VIRTUAL_HEIGHT = 1080f

    // Physics
    const val PIXELS_PER_METER = 100f
    const val GRAVITY = -25f
    const val TIME_STEP = 1f / 60f
    const val VELOCITY_ITERATIONS = 8
    const val POSITION_ITERATIONS = 3

    // Combat
    const val ROUND_TIME = 99 // seconds
    const val ROUNDS_TO_WIN = 2
    const val MAX_ROUNDS = 3

    // Character
    const val PLAYER_WALK_SPEED = 8f
    const val PLAYER_RUN_SPEED = 14f
    const val PLAYER_JUMP_FORCE = 18f
    const val PLAYER_MAX_HEALTH = 100f
    const val PLAYER_MAX_ENERGY = 100f

    // Combat timing (frames at 60fps)
    const val LIGHT_ATTACK_STARTUP = 4
    const val LIGHT_ATTACK_ACTIVE = 3
    const val LIGHT_ATTACK_RECOVERY = 8
    const val HEAVY_ATTACK_STARTUP = 10
    const val HEAVY_ATTACK_ACTIVE = 5
    const val HEAVY_ATTACK_RECOVERY = 15
    const val BLOCK_STARTUP = 2

    // Damage
    const val PUNCH_DAMAGE = 8f
    const val KICK_DAMAGE = 12f
    const val WEAPON_DAMAGE_MULTIPLIER = 1.5f
    const val CRITICAL_HIT_MULTIPLIER = 2.0f
    const val BLOCK_DAMAGE_REDUCTION = 0.8f

    // Combo
    const val COMBO_WINDOW_MS = 500L
    const val MAX_COMBO_HITS = 10

    // Animation
    const val FRAME_DURATION = 0.08f
    const val HIT_STUN_DURATION = 0.3f
    const val KNOCKBACK_FORCE = 10f

    // AI
    const val AI_REACTION_TIME_MIN = 0.1f
    const val AI_REACTION_TIME_MAX = 0.4f
    const val AI_AGGRESSION_BASE = 0.5f

    // Acts
    const val TOTAL_ACTS = 7
    val ACT_NAMES = arrayOf(
        "Hermit", "Butcher", "Wasp", "Widow", "Shogun", "Titan", "Gates of Shadows"
    )
}
