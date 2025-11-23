package com.shadowfight.game.combat

import com.shadowfight.game.utils.GameConfig

/**
 * Defines attack properties and types
 */
data class Attack(
    val name: String,
    val type: AttackType,
    val damage: Float,
    val range: Float,
    val hitboxHeight: Float,
    val startupFrames: Int,
    val activeFrames: Int,
    val recoveryFrames: Int,
    val energyCost: Float,
    val knockbackX: Float,
    val knockbackY: Float,
    val canCombo: Boolean = true,
    val comboNextAttacks: List<String> = emptyList()
) {
    val totalFrames: Int get() = startupFrames + activeFrames + recoveryFrames
}

enum class AttackType {
    PUNCH,
    KICK,
    WEAPON,
    RANGED,
    MAGIC,
    SPECIAL
}

/**
 * Predefined attacks catalog
 */
object Attacks {
    // Basic punches
    val JAB = Attack(
        name = "Jab",
        type = AttackType.PUNCH,
        damage = 5f,
        range = 60f,
        hitboxHeight = 40f,
        startupFrames = 3,
        activeFrames = 2,
        recoveryFrames = 5,
        energyCost = 0f,
        knockbackX = 2f,
        knockbackY = 0f,
        comboNextAttacks = listOf("Straight", "Hook")
    )

    val STRAIGHT = Attack(
        name = "Straight",
        type = AttackType.PUNCH,
        damage = 8f,
        range = 70f,
        hitboxHeight = 40f,
        startupFrames = 4,
        activeFrames = 3,
        recoveryFrames = 8,
        energyCost = 0f,
        knockbackX = 4f,
        knockbackY = 0f,
        comboNextAttacks = listOf("Hook", "Uppercut")
    )

    val HOOK = Attack(
        name = "Hook",
        type = AttackType.PUNCH,
        damage = 10f,
        range = 55f,
        hitboxHeight = 45f,
        startupFrames = 6,
        activeFrames = 3,
        recoveryFrames = 10,
        energyCost = 0f,
        knockbackX = 6f,
        knockbackY = 1f
    )

    val UPPERCUT = Attack(
        name = "Uppercut",
        type = AttackType.PUNCH,
        damage = 15f,
        range = 50f,
        hitboxHeight = 60f,
        startupFrames = 8,
        activeFrames = 4,
        recoveryFrames = 12,
        energyCost = 5f,
        knockbackX = 3f,
        knockbackY = 8f
    )

    // Basic kicks
    val LOW_KICK = Attack(
        name = "Low Kick",
        type = AttackType.KICK,
        damage = 7f,
        range = 80f,
        hitboxHeight = 30f,
        startupFrames = 4,
        activeFrames = 3,
        recoveryFrames = 7,
        energyCost = 0f,
        knockbackX = 3f,
        knockbackY = 0f,
        comboNextAttacks = listOf("Middle Kick", "High Kick")
    )

    val MIDDLE_KICK = Attack(
        name = "Middle Kick",
        type = AttackType.KICK,
        damage = 12f,
        range = 90f,
        hitboxHeight = 40f,
        startupFrames = 5,
        activeFrames = 4,
        recoveryFrames = 9,
        energyCost = 0f,
        knockbackX = 5f,
        knockbackY = 0f,
        comboNextAttacks = listOf("High Kick", "Spinning Kick")
    )

    val HIGH_KICK = Attack(
        name = "High Kick",
        type = AttackType.KICK,
        damage = 15f,
        range = 85f,
        hitboxHeight = 50f,
        startupFrames = 7,
        activeFrames = 4,
        recoveryFrames = 12,
        energyCost = 5f,
        knockbackX = 6f,
        knockbackY = 2f
    )

    val SPINNING_KICK = Attack(
        name = "Spinning Kick",
        type = AttackType.KICK,
        damage = 20f,
        range = 100f,
        hitboxHeight = 60f,
        startupFrames = 10,
        activeFrames = 5,
        recoveryFrames = 15,
        energyCost = 10f,
        knockbackX = 10f,
        knockbackY = 3f
    )

    // Jump attacks
    val JUMP_PUNCH = Attack(
        name = "Jump Punch",
        type = AttackType.PUNCH,
        damage = 10f,
        range = 65f,
        hitboxHeight = 50f,
        startupFrames = 3,
        activeFrames = 5,
        recoveryFrames = 6,
        energyCost = 0f,
        knockbackX = 4f,
        knockbackY = -2f
    )

    val JUMP_KICK = Attack(
        name = "Jump Kick",
        type = AttackType.KICK,
        damage = 14f,
        range = 90f,
        hitboxHeight = 55f,
        startupFrames = 4,
        activeFrames = 6,
        recoveryFrames = 8,
        energyCost = 0f,
        knockbackX = 6f,
        knockbackY = -3f
    )

    val FLYING_KICK = Attack(
        name = "Flying Kick",
        type = AttackType.KICK,
        damage = 22f,
        range = 120f,
        hitboxHeight = 60f,
        startupFrames = 6,
        activeFrames = 8,
        recoveryFrames = 15,
        energyCost = 15f,
        knockbackX = 12f,
        knockbackY = -4f
    )

    // Special moves
    val SWEEP = Attack(
        name = "Sweep",
        type = AttackType.KICK,
        damage = 12f,
        range = 100f,
        hitboxHeight = 25f,
        startupFrames = 8,
        activeFrames = 5,
        recoveryFrames = 18,
        energyCost = 10f,
        knockbackX = 2f,
        knockbackY = 6f
    )

    val THROW = Attack(
        name = "Throw",
        type = AttackType.SPECIAL,
        damage = 25f,
        range = 40f,
        hitboxHeight = 100f,
        startupFrames = 10,
        activeFrames = 8,
        recoveryFrames = 20,
        energyCost = 20f,
        knockbackX = 15f,
        knockbackY = 5f,
        canCombo = false
    )

    // Get attack by name
    fun getAttack(name: String): Attack? {
        return when (name.lowercase()) {
            "jab" -> JAB
            "straight" -> STRAIGHT
            "hook" -> HOOK
            "uppercut" -> UPPERCUT
            "low kick", "lowkick" -> LOW_KICK
            "middle kick", "middlekick" -> MIDDLE_KICK
            "high kick", "highkick" -> HIGH_KICK
            "spinning kick", "spinningkick" -> SPINNING_KICK
            "jump punch", "jumppunch" -> JUMP_PUNCH
            "jump kick", "jumpkick" -> JUMP_KICK
            "flying kick", "flyingkick" -> FLYING_KICK
            "sweep" -> SWEEP
            "throw" -> THROW
            else -> null
        }
    }
}
