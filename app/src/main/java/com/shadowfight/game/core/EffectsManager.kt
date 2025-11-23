package com.shadowfight.game.core

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2

/**
 * Manages visual effects like particles, hit sparks, etc.
 */
class EffectsManager {

    private val particles = mutableListOf<Particle>()
    private val hitSparks = mutableListOf<HitSpark>()
    private val trailEffects = mutableListOf<TrailEffect>()
    private val textEffects = mutableListOf<FloatingText>()

    /**
     * Update all active effects
     */
    fun update(deltaTime: Float) {
        // Update particles
        particles.forEach { it.update(deltaTime) }
        particles.removeAll { !it.isAlive }

        // Update hit sparks
        hitSparks.forEach { it.update(deltaTime) }
        hitSparks.removeAll { !it.isAlive }

        // Update trails
        trailEffects.forEach { it.update(deltaTime) }
        trailEffects.removeAll { !it.isAlive }

        // Update text effects
        textEffects.forEach { it.update(deltaTime) }
        textEffects.removeAll { !it.isAlive }
    }

    /**
     * Render all effects
     */
    fun render(batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        // Draw trails first (behind everything)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        trailEffects.forEach { it.render(shapeRenderer) }
        shapeRenderer.end()

        // Draw particles
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        particles.forEach { it.render(shapeRenderer) }
        shapeRenderer.end()

        // Draw hit sparks
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        hitSparks.forEach { it.render(shapeRenderer) }
        shapeRenderer.end()
    }

    /**
     * Create hit spark effect at impact point
     */
    fun createHitSpark(position: Vector2, isCritical: Boolean = false) {
        val spark = HitSpark(
            position = position.cpy(),
            color = if (isCritical) Color.YELLOW else Color.WHITE,
            size = if (isCritical) 80f else 50f
        )
        hitSparks.add(spark)

        // Add particles around the hit
        val particleCount = if (isCritical) 15 else 8
        for (i in 0 until particleCount) {
            val angle = MathUtils.random(360f)
            val speed = MathUtils.random(100f, 300f)
            val velocity = Vector2(
                MathUtils.cosDeg(angle) * speed,
                MathUtils.sinDeg(angle) * speed
            )
            particles.add(Particle(
                position = position.cpy(),
                velocity = velocity,
                color = if (isCritical) Color.ORANGE else Color.WHITE,
                size = MathUtils.random(3f, 8f),
                lifetime = MathUtils.random(0.2f, 0.5f)
            ))
        }
    }

    /**
     * Create block effect
     */
    fun createBlockEffect(position: Vector2) {
        val spark = HitSpark(
            position = position.cpy(),
            color = Color.CYAN,
            size = 40f
        )
        hitSparks.add(spark)

        // Shield-like particles
        for (i in 0 until 6) {
            val angle = MathUtils.random(360f)
            val speed = MathUtils.random(50f, 150f)
            particles.add(Particle(
                position = position.cpy(),
                velocity = Vector2(MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed),
                color = Color.CYAN,
                size = MathUtils.random(4f, 10f),
                lifetime = 0.3f
            ))
        }
    }

    /**
     * Create dust effect for movement/landing
     */
    fun createDustEffect(position: Vector2, direction: Float = 0f) {
        for (i in 0 until 5) {
            val offsetX = MathUtils.random(-20f, 20f)
            val speed = MathUtils.random(30f, 80f)
            particles.add(Particle(
                position = Vector2(position.x + offsetX, position.y),
                velocity = Vector2(direction * speed * 0.5f, MathUtils.random(20f, 60f)),
                color = Color(0.4f, 0.35f, 0.3f, 0.6f),
                size = MathUtils.random(8f, 15f),
                lifetime = MathUtils.random(0.3f, 0.6f),
                fadeOut = true
            ))
        }
    }

    /**
     * Create weapon trail effect
     */
    fun createWeaponTrail(startPos: Vector2, endPos: Vector2, color: Color = Color.WHITE) {
        trailEffects.add(TrailEffect(
            startPosition = startPos.cpy(),
            endPosition = endPos.cpy(),
            color = color,
            width = 15f,
            lifetime = 0.15f
        ))
    }

    /**
     * Create magic effect
     */
    fun createMagicEffect(position: Vector2, type: MagicEffectType) {
        val color = when (type) {
            MagicEffectType.FIRE -> Color.ORANGE
            MagicEffectType.ICE -> Color.CYAN
            MagicEffectType.LIGHTNING -> Color.YELLOW
            MagicEffectType.SHADOW -> Color.PURPLE
            MagicEffectType.HEAL -> Color.GREEN
        }

        // Create burst of particles
        for (i in 0 until 20) {
            val angle = MathUtils.random(360f)
            val speed = MathUtils.random(80f, 200f)
            particles.add(Particle(
                position = position.cpy(),
                velocity = Vector2(MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed),
                color = color.cpy(),
                size = MathUtils.random(5f, 12f),
                lifetime = MathUtils.random(0.4f, 0.8f),
                fadeOut = true
            ))
        }
    }

    /**
     * Create floating damage text
     */
    fun createDamageText(position: Vector2, damage: Int, isCritical: Boolean = false) {
        val color = when {
            isCritical -> Color.YELLOW
            damage > 20 -> Color.RED
            else -> Color.WHITE
        }
        val text = if (isCritical) "$damage!" else damage.toString()

        textEffects.add(FloatingText(
            position = position.cpy(),
            text = text,
            color = color,
            scale = if (isCritical) 2f else 1.5f,
            lifetime = 1.5f
        ))
    }

    /**
     * Clear all effects
     */
    fun clear() {
        particles.clear()
        hitSparks.clear()
        trailEffects.clear()
        textEffects.clear()
    }
}

/**
 * Basic particle class
 */
class Particle(
    var position: Vector2,
    var velocity: Vector2,
    var color: Color,
    var size: Float,
    var lifetime: Float,
    var fadeOut: Boolean = true
) {
    private val maxLifetime = lifetime
    val isAlive: Boolean get() = lifetime > 0

    fun update(deltaTime: Float) {
        position.add(velocity.x * deltaTime, velocity.y * deltaTime)
        velocity.scl(0.95f) // Friction
        velocity.y -= 500f * deltaTime // Gravity
        lifetime -= deltaTime
    }

    fun render(shapeRenderer: ShapeRenderer) {
        val alpha = if (fadeOut) lifetime / maxLifetime else 1f
        shapeRenderer.color = Color(color.r, color.g, color.b, alpha * color.a)
        shapeRenderer.circle(position.x, position.y, size * (0.5f + 0.5f * alpha))
    }
}

/**
 * Hit spark effect
 */
class HitSpark(
    val position: Vector2,
    val color: Color,
    var size: Float,
    var lifetime: Float = 0.15f
) {
    private val maxLifetime = lifetime
    val isAlive: Boolean get() = lifetime > 0

    fun update(deltaTime: Float) {
        lifetime -= deltaTime
        size *= 1.1f // Expand
    }

    fun render(shapeRenderer: ShapeRenderer) {
        val alpha = lifetime / maxLifetime
        shapeRenderer.color = Color(color.r, color.g, color.b, alpha)

        // Draw star-like spark
        val rays = 8
        for (i in 0 until rays) {
            val angle = (i * 360f / rays) + (lifetime * 500f)
            val innerRadius = size * 0.3f
            val outerRadius = size

            val x1 = position.x + MathUtils.cosDeg(angle) * innerRadius
            val y1 = position.y + MathUtils.sinDeg(angle) * innerRadius
            val x2 = position.x + MathUtils.cosDeg(angle) * outerRadius
            val y2 = position.y + MathUtils.sinDeg(angle) * outerRadius

            shapeRenderer.line(x1, y1, x2, y2)
        }
    }
}

/**
 * Trail effect for weapon swings
 */
class TrailEffect(
    val startPosition: Vector2,
    val endPosition: Vector2,
    val color: Color,
    var width: Float,
    var lifetime: Float
) {
    private val maxLifetime = lifetime
    val isAlive: Boolean get() = lifetime > 0

    fun update(deltaTime: Float) {
        lifetime -= deltaTime
    }

    fun render(shapeRenderer: ShapeRenderer) {
        val alpha = lifetime / maxLifetime
        shapeRenderer.color = Color(color.r, color.g, color.b, alpha * 0.5f)
        shapeRenderer.rectLine(startPosition, endPosition, width * alpha)
    }
}

/**
 * Floating text for damage numbers
 */
class FloatingText(
    var position: Vector2,
    val text: String,
    val color: Color,
    var scale: Float,
    var lifetime: Float
) {
    private val maxLifetime = lifetime
    val isAlive: Boolean get() = lifetime > 0

    fun update(deltaTime: Float) {
        position.y += 100f * deltaTime
        lifetime -= deltaTime
    }
}

enum class MagicEffectType {
    FIRE,
    ICE,
    LIGHTNING,
    SHADOW,
    HEAL
}
