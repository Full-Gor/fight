package com.shadowfight.game.combat

/**
 * Weapon definitions with stats and special properties
 */
enum class Weapon(
    val displayName: String,
    val damageMultiplier: Float,
    val rangeBonus: Float,
    val speedModifier: Float,
    val price: Int,
    val requiredLevel: Int
) {
    FISTS("Fists", 1.0f, 0f, 1.0f, 0, 1),
    WOODEN_SWORD("Wooden Sword", 1.2f, 20f, 0.95f, 100, 1),
    KNIVES("Knives", 1.3f, 15f, 1.1f, 250, 2),
    NUNCHAKU("Nunchaku", 1.4f, 25f, 1.05f, 500, 3),
    SAIS("Sais", 1.5f, 18f, 1.0f, 750, 4),
    KATANA("Katana", 1.8f, 40f, 0.9f, 1500, 5),
    KUSARIGAMA("Kusarigama", 1.7f, 60f, 0.85f, 2000, 6),
    STAFF("Staff", 1.6f, 50f, 0.92f, 1800, 5),
    DUAL_SWORDS("Dual Swords", 2.0f, 35f, 1.0f, 3000, 7),
    BLOOD_REAPER("Blood Reaper", 2.2f, 55f, 0.88f, 5000, 10),
    HERMIT_SWORDS("Hermit's Swords", 2.5f, 45f, 1.05f, 0, 1), // Boss weapon
    BUTCHER_KNIVES("Butcher's Knives", 2.6f, 30f, 1.1f, 0, 1), // Boss weapon
    SHOGUN_KATANA("Shogun's Katana", 3.0f, 50f, 0.95f, 0, 1), // Boss weapon
    TITAN_SWORD("Titan's Desolator", 3.5f, 70f, 0.8f, 0, 1); // Final boss weapon

    companion object {
        fun getByName(name: String): Weapon {
            return values().find { it.name.equals(name, ignoreCase = true) } ?: FISTS
        }

        fun getAvailableWeapons(level: Int, coins: Int): List<Weapon> {
            return values().filter { it.requiredLevel <= level && it.price <= coins && it.price > 0 }
        }
    }
}

/**
 * Armor definitions with defense bonuses
 */
enum class Armor(
    val displayName: String,
    val defenseBonus: Float,
    val healthBonus: Float,
    val price: Int,
    val requiredLevel: Int
) {
    BASIC("Basic Cloth", 0f, 0f, 0, 1),
    LEATHER("Leather Armor", 5f, 10f, 200, 2),
    CHAINMAIL("Chainmail", 10f, 20f, 500, 3),
    SAMURAI("Samurai Armor", 15f, 30f, 1000, 5),
    NINJA("Ninja Suit", 8f, 15f, 800, 4),
    DEMON_ARMOR("Demon Armor", 20f, 40f, 2500, 7),
    SHADOW_ARMOR("Shadow Armor", 25f, 50f, 5000, 10),
    TITAN_ARMOR("Titan's Armor", 35f, 75f, 0, 1); // Boss armor

    companion object {
        fun getByName(name: String): Armor {
            return values().find { it.name.equals(name, ignoreCase = true) } ?: BASIC
        }
    }
}

/**
 * Helm definitions
 */
enum class Helm(
    val displayName: String,
    val defenseBonus: Float,
    val specialEffect: String,
    val price: Int,
    val requiredLevel: Int
) {
    NONE("None", 0f, "", 0, 1),
    BANDANA("Bandana", 2f, "", 100, 1),
    LEATHER_HELM("Leather Helm", 5f, "", 300, 2),
    SAMURAI_HELM("Samurai Helm", 10f, "", 800, 4),
    DEMON_MASK("Demon Mask", 15f, "Intimidation", 2000, 6),
    SHADOW_HELM("Shadow Helm", 20f, "Shadow Vision", 4000, 9);

    companion object {
        fun getByName(name: String): Helm {
            return values().find { it.name.equals(name, ignoreCase = true) } ?: NONE
        }
    }
}

/**
 * Ranged weapon definitions
 */
enum class RangedWeapon(
    val displayName: String,
    val damage: Float,
    val range: Float,
    val cooldown: Float,
    val price: Int,
    val requiredLevel: Int
) {
    NONE("None", 0f, 0f, 0f, 0, 1),
    KUNAI("Kunai", 10f, 400f, 3f, 300, 2),
    SHURIKEN("Shuriken", 8f, 500f, 2f, 400, 3),
    NEEDLES("Needles", 15f, 350f, 4f, 600, 4),
    BOMBS("Smoke Bombs", 20f, 300f, 5f, 1000, 5),
    CHAKRAM("Chakram", 25f, 600f, 4f, 2000, 7);

    companion object {
        fun getByName(name: String): RangedWeapon {
            return values().find { it.name.equals(name, ignoreCase = true) } ?: NONE
        }
    }
}

/**
 * Magic abilities
 */
enum class Magic(
    val displayName: String,
    val damage: Float,
    val energyCost: Float,
    val cooldown: Float,
    val effect: MagicEffect,
    val price: Int,
    val requiredLevel: Int
) {
    NONE("None", 0f, 0f, 0f, MagicEffect.NONE, 0, 1),
    FIREBALL("Fireball", 30f, 25f, 5f, MagicEffect.BURN, 500, 3),
    LIGHTNING("Lightning", 35f, 30f, 6f, MagicEffect.STUN, 800, 4),
    ICE_BALL("Ice Ball", 25f, 20f, 4f, MagicEffect.SLOW, 600, 3),
    FRENZY("Frenzy", 0f, 40f, 15f, MagicEffect.BUFF_ATTACK, 1000, 5),
    SHIELD("Energy Shield", 0f, 35f, 12f, MagicEffect.BUFF_DEFENSE, 1200, 6),
    DRAIN("Life Drain", 20f, 30f, 8f, MagicEffect.LIFESTEAL, 2000, 8),
    SHADOW_BLAST("Shadow Blast", 50f, 50f, 10f, MagicEffect.KNOCKDOWN, 5000, 10);

    companion object {
        fun getByName(name: String): Magic {
            return values().find { it.name.equals(name, ignoreCase = true) } ?: NONE
        }
    }
}

enum class MagicEffect {
    NONE,
    BURN,      // Damage over time
    STUN,      // Brief stun
    SLOW,      // Reduced speed
    KNOCKDOWN, // Knocks down enemy
    BUFF_ATTACK,   // Increases attack
    BUFF_DEFENSE,  // Increases defense
    LIFESTEAL      // Heals on hit
}
