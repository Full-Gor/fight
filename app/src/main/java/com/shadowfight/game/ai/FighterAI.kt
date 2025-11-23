package com.shadowfight.game.ai

import com.badlogic.gdx.math.MathUtils
import com.shadowfight.game.entities.Fighter
import com.shadowfight.game.entities.FighterState
import com.shadowfight.game.combat.*
import com.shadowfight.game.utils.GameConfig
import kotlin.math.abs

/**
 * AI controller for enemy fighters
 * Implements various fighting strategies based on difficulty
 */
class FighterAI(
    private val fighter: Fighter,
    private val opponent: Fighter,
    private val difficulty: Float // 0.0 to 1.0
) {
    // AI state
    private var currentStrategy: AIStrategy = AIStrategy.NEUTRAL
    private var strategyTimer: Float = 0f
    private var reactionTimer: Float = 0f
    private var actionCooldown: Float = 0f
    private var comboCounter: Int = 0

    // Decision weights influenced by difficulty
    private val reactionTime: Float = GameConfig.AI_REACTION_TIME_MAX -
        (GameConfig.AI_REACTION_TIME_MAX - GameConfig.AI_REACTION_TIME_MIN) * difficulty
    private val aggressionLevel: Float = GameConfig.AI_AGGRESSION_BASE + difficulty * 0.4f
    private val blockChance: Float = 0.3f + difficulty * 0.5f
    private val comboChance: Float = 0.2f + difficulty * 0.6f

    fun update(deltaTime: Float) {
        // Update timers
        if (reactionTimer > 0) reactionTimer -= deltaTime
        if (actionCooldown > 0) actionCooldown -= deltaTime
        strategyTimer -= deltaTime

        // Can't act if stunned or already attacking
        if (fighter.isHitStunned || fighter.currentAttack != null) {
            return
        }

        // Update strategy periodically
        if (strategyTimer <= 0) {
            updateStrategy()
            strategyTimer = MathUtils.random(0.5f, 1.5f)
        }

        // Execute current strategy
        when (currentStrategy) {
            AIStrategy.AGGRESSIVE -> executeAggressiveStrategy(deltaTime)
            AIStrategy.DEFENSIVE -> executeDefensiveStrategy(deltaTime)
            AIStrategy.NEUTRAL -> executeNeutralStrategy(deltaTime)
            AIStrategy.COMBO -> executeComboStrategy(deltaTime)
            AIStrategy.RETREAT -> executeRetreatStrategy(deltaTime)
        }
    }

    private fun updateStrategy() {
        val distanceToOpponent = getDistanceToOpponent()
        val healthPercentage = fighter.health / fighter.maxHealth
        val opponentHealthPercentage = opponent.health / opponent.maxHealth

        // Choose strategy based on situation
        currentStrategy = when {
            // Low health - be more defensive
            healthPercentage < 0.3f -> {
                if (MathUtils.randomBoolean(0.6f)) AIStrategy.DEFENSIVE
                else AIStrategy.RETREAT
            }
            // Opponent low health - be aggressive
            opponentHealthPercentage < 0.3f -> AIStrategy.AGGRESSIVE
            // Opponent is attacking - decide to block or counter
            opponent.state == FighterState.ATTACKING -> {
                if (MathUtils.randomBoolean(blockChance)) AIStrategy.DEFENSIVE
                else AIStrategy.NEUTRAL
            }
            // Close range - mix of strategies
            distanceToOpponent < 150f -> {
                when {
                    MathUtils.randomBoolean(aggressionLevel) -> AIStrategy.AGGRESSIVE
                    MathUtils.randomBoolean(comboChance) -> AIStrategy.COMBO
                    else -> AIStrategy.NEUTRAL
                }
            }
            // Mid range - approach or wait
            distanceToOpponent < 400f -> {
                if (MathUtils.randomBoolean(aggressionLevel)) AIStrategy.AGGRESSIVE
                else AIStrategy.NEUTRAL
            }
            // Far range - approach
            else -> AIStrategy.AGGRESSIVE
        }
    }

    private fun executeAggressiveStrategy(deltaTime: Float) {
        val distance = getDistanceToOpponent()

        when {
            // In attack range
            distance < getAttackRange() -> {
                if (actionCooldown <= 0 && reactionTimer <= 0) {
                    performAttack()
                    actionCooldown = MathUtils.random(0.2f, 0.5f) / difficulty
                }
            }
            // Need to close distance
            else -> {
                moveTowardsOpponent()

                // Occasionally jump attack
                if (distance < 300f && MathUtils.randomBoolean(0.02f * difficulty)) {
                    fighter.jump()
                }
            }
        }
    }

    private fun executeDefensiveStrategy(deltaTime: Float) {
        val distance = getDistanceToOpponent()

        // Block if opponent is attacking and close
        if (opponent.state == FighterState.ATTACKING && distance < 200f) {
            if (reactionTimer <= 0) {
                fighter.startBlocking()
                reactionTimer = reactionTime
            }
        } else {
            fighter.stopBlocking()

            // Counter attack opportunity
            if (opponent.state == FighterState.RECOVERING && distance < getAttackRange()) {
                if (MathUtils.randomBoolean(difficulty)) {
                    performAttack()
                }
            }

            // Keep distance
            if (distance < 150f) {
                moveAwayFromOpponent()
            }
        }
    }

    private fun executeNeutralStrategy(deltaTime: Float) {
        val distance = getDistanceToOpponent()

        // Optimal fighting distance
        val optimalDistance = 180f

        when {
            distance < optimalDistance - 50f -> moveAwayFromOpponent()
            distance > optimalDistance + 50f -> moveTowardsOpponent()
            else -> {
                fighter.stopMoving()

                // Occasional poke attack
                if (MathUtils.randomBoolean(0.03f * difficulty) && actionCooldown <= 0) {
                    performAttack()
                    actionCooldown = MathUtils.random(0.3f, 0.8f)
                }
            }
        }

        // React to opponent attacks
        if (opponent.state == FighterState.ATTACKING && MathUtils.randomBoolean(blockChance)) {
            fighter.startBlocking()
        } else {
            fighter.stopBlocking()
        }
    }

    private fun executeComboStrategy(deltaTime: Float) {
        val distance = getDistanceToOpponent()

        if (distance < getAttackRange()) {
            if (actionCooldown <= 0) {
                // Perform combo attack
                val attack = getComboAttack(comboCounter)
                if (fighter.startAttack(attack)) {
                    comboCounter++
                    actionCooldown = 0.1f // Quick follow-up for combos

                    // Reset combo after max hits or random chance
                    if (comboCounter >= 4 || MathUtils.randomBoolean(0.3f)) {
                        comboCounter = 0
                        currentStrategy = AIStrategy.NEUTRAL
                    }
                }
            }
        } else {
            moveTowardsOpponent()
            comboCounter = 0
        }
    }

    private fun executeRetreatStrategy(deltaTime: Float) {
        moveAwayFromOpponent()

        // Block while retreating
        if (opponent.state == FighterState.ATTACKING) {
            fighter.startBlocking()
        } else {
            fighter.stopBlocking()
        }

        // Counter if opportunity arises
        if (opponent.state == FighterState.RECOVERING && MathUtils.randomBoolean(0.3f * difficulty)) {
            currentStrategy = AIStrategy.AGGRESSIVE
        }
    }

    private fun performAttack() {
        val distance = getDistanceToOpponent()
        val heightDiff = opponent.position.y - fighter.position.y

        val attack = when {
            // Air attack if jumping
            !fighter.isGrounded -> if (MathUtils.randomBoolean()) Attacks.JUMP_PUNCH else Attacks.JUMP_KICK

            // Sweep for low attacks or to knock down
            opponent.state == FighterState.CROUCHING || MathUtils.randomBoolean(0.1f) -> Attacks.SWEEP

            // High kick for more damage
            distance < 100f && MathUtils.randomBoolean(difficulty * 0.3f) -> Attacks.HIGH_KICK

            // Mix of punches and kicks
            MathUtils.randomBoolean(0.4f) -> {
                when (MathUtils.random(2)) {
                    0 -> Attacks.JAB
                    1 -> Attacks.STRAIGHT
                    else -> Attacks.HOOK
                }
            }
            else -> {
                when (MathUtils.random(2)) {
                    0 -> Attacks.LOW_KICK
                    1 -> Attacks.MIDDLE_KICK
                    else -> Attacks.HIGH_KICK
                }
            }
        }

        fighter.startAttack(attack)
    }

    private fun getComboAttack(index: Int): Attack {
        return when (index % 5) {
            0 -> Attacks.JAB
            1 -> Attacks.STRAIGHT
            2 -> Attacks.LOW_KICK
            3 -> Attacks.MIDDLE_KICK
            4 -> Attacks.HIGH_KICK
            else -> Attacks.JAB
        }
    }

    private fun moveTowardsOpponent() {
        if (opponent.position.x > fighter.position.x) {
            fighter.moveRight()
        } else {
            fighter.moveLeft()
        }
    }

    private fun moveAwayFromOpponent() {
        if (opponent.position.x > fighter.position.x) {
            fighter.moveLeft()
        } else {
            fighter.moveRight()
        }
    }

    private fun getDistanceToOpponent(): Float {
        return abs(fighter.position.x - opponent.position.x)
    }

    private fun getAttackRange(): Float {
        return 150f + fighter.equippedWeapon.rangeBonus
    }
}

enum class AIStrategy {
    AGGRESSIVE,  // Close distance and attack frequently
    DEFENSIVE,   // Block and counter-attack
    NEUTRAL,     // Maintain distance, poke occasionally
    COMBO,       // Chain multiple attacks together
    RETREAT      // Back away and recover
}
