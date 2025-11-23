package com.shadowfight.game.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Disposable
import kotlin.math.sin
import kotlin.math.cos

/**
 * Controls character animations
 * Generates procedural silhouette animations for Shadow Fight style
 */
class AnimationController : Disposable {

    private val animations = mutableMapOf<AnimationType, Animation<TextureRegion>>()
    private val generatedTextures = mutableListOf<Texture>()

    var currentAnimation: AnimationType = AnimationType.IDLE
        private set
    var stateTime: Float = 0f
        private set
    var isAnimationFinished: Boolean = false
        private set

    init {
        generateAllAnimations()
    }

    private fun generateAllAnimations() {
        // Generate idle animation
        animations[AnimationType.IDLE] = generateIdleAnimation()

        // Generate walk animation
        animations[AnimationType.WALK] = generateWalkAnimation()

        // Generate jump animation
        animations[AnimationType.JUMP] = generateJumpAnimation()

        // Generate attack animations
        animations[AnimationType.PUNCH] = generatePunchAnimation()
        animations[AnimationType.KICK] = generateKickAnimation()
        animations[AnimationType.HIGH_KICK] = generateHighKickAnimation()

        // Generate block animation
        animations[AnimationType.BLOCK] = generateBlockAnimation()

        // Generate hit stun animation
        animations[AnimationType.HIT_STUN] = generateHitStunAnimation()

        // Generate crouch animation
        animations[AnimationType.CROUCH] = generateCrouchAnimation()
    }

    private fun generateIdleAnimation(): Animation<TextureRegion> {
        val frames = mutableListOf<TextureRegion>()
        val frameCount = 8

        for (i in 0 until frameCount) {
            val phase = i.toFloat() / frameCount * Math.PI * 2
            val breathOffset = (sin(phase) * 3).toFloat()

            val texture = createSilhouetteFrame(128, 256) { pixmap ->
                drawStandingPose(pixmap, breathOffset)
            }
            generatedTextures.add(texture)
            frames.add(TextureRegion(texture))
        }

        return Animation(0.1f, com.badlogic.gdx.utils.Array(frames.toTypedArray()), Animation.PlayMode.LOOP)
    }

    private fun generateWalkAnimation(): Animation<TextureRegion> {
        val frames = mutableListOf<TextureRegion>()
        val frameCount = 8

        for (i in 0 until frameCount) {
            val phase = i.toFloat() / frameCount * Math.PI * 2
            val legOffset = (sin(phase) * 25).toFloat()
            val armOffset = (sin(phase + Math.PI) * 15).toFloat()

            val texture = createSilhouetteFrame(128, 256) { pixmap ->
                drawWalkingPose(pixmap, legOffset, armOffset)
            }
            generatedTextures.add(texture)
            frames.add(TextureRegion(texture))
        }

        return Animation(0.08f, com.badlogic.gdx.utils.Array(frames.toTypedArray()), Animation.PlayMode.LOOP)
    }

    private fun generateJumpAnimation(): Animation<TextureRegion> {
        val frames = mutableListOf<TextureRegion>()

        // Jump anticipation, air, landing
        val phases = listOf(0f, 0.3f, 0.6f, 1f)

        for (phase in phases) {
            val texture = createSilhouetteFrame(128, 256) { pixmap ->
                drawJumpPose(pixmap, phase)
            }
            generatedTextures.add(texture)
            frames.add(TextureRegion(texture))
        }

        return Animation(0.1f, com.badlogic.gdx.utils.Array(frames.toTypedArray()), Animation.PlayMode.NORMAL)
    }

    private fun generatePunchAnimation(): Animation<TextureRegion> {
        val frames = mutableListOf<TextureRegion>()
        val frameCount = 6

        for (i in 0 until frameCount) {
            val phase = i.toFloat() / (frameCount - 1)

            val texture = createSilhouetteFrame(160, 256) { pixmap ->
                drawPunchPose(pixmap, phase)
            }
            generatedTextures.add(texture)
            frames.add(TextureRegion(texture))
        }

        return Animation(0.05f, com.badlogic.gdx.utils.Array(frames.toTypedArray()), Animation.PlayMode.NORMAL)
    }

    private fun generateKickAnimation(): Animation<TextureRegion> {
        val frames = mutableListOf<TextureRegion>()
        val frameCount = 8

        for (i in 0 until frameCount) {
            val phase = i.toFloat() / (frameCount - 1)

            val texture = createSilhouetteFrame(180, 256) { pixmap ->
                drawKickPose(pixmap, phase)
            }
            generatedTextures.add(texture)
            frames.add(TextureRegion(texture))
        }

        return Animation(0.04f, com.badlogic.gdx.utils.Array(frames.toTypedArray()), Animation.PlayMode.NORMAL)
    }

    private fun generateHighKickAnimation(): Animation<TextureRegion> {
        val frames = mutableListOf<TextureRegion>()
        val frameCount = 10

        for (i in 0 until frameCount) {
            val phase = i.toFloat() / (frameCount - 1)

            val texture = createSilhouetteFrame(160, 280) { pixmap ->
                drawHighKickPose(pixmap, phase)
            }
            generatedTextures.add(texture)
            frames.add(TextureRegion(texture))
        }

        return Animation(0.04f, com.badlogic.gdx.utils.Array(frames.toTypedArray()), Animation.PlayMode.NORMAL)
    }

    private fun generateBlockAnimation(): Animation<TextureRegion> {
        val frames = mutableListOf<TextureRegion>()

        val texture = createSilhouetteFrame(128, 256) { pixmap ->
            drawBlockPose(pixmap)
        }
        generatedTextures.add(texture)
        frames.add(TextureRegion(texture))

        return Animation(0.1f, com.badlogic.gdx.utils.Array(frames.toTypedArray()), Animation.PlayMode.LOOP)
    }

    private fun generateHitStunAnimation(): Animation<TextureRegion> {
        val frames = mutableListOf<TextureRegion>()
        val frameCount = 4

        for (i in 0 until frameCount) {
            val phase = i.toFloat() / (frameCount - 1)

            val texture = createSilhouetteFrame(140, 256) { pixmap ->
                drawHitStunPose(pixmap, phase)
            }
            generatedTextures.add(texture)
            frames.add(TextureRegion(texture))
        }

        return Animation(0.08f, com.badlogic.gdx.utils.Array(frames.toTypedArray()), Animation.PlayMode.NORMAL)
    }

    private fun generateCrouchAnimation(): Animation<TextureRegion> {
        val frames = mutableListOf<TextureRegion>()

        val texture = createSilhouetteFrame(140, 180) { pixmap ->
            drawCrouchPose(pixmap)
        }
        generatedTextures.add(texture)
        frames.add(TextureRegion(texture))

        return Animation(0.1f, com.badlogic.gdx.utils.Array(frames.toTypedArray()), Animation.PlayMode.LOOP)
    }

    private fun createSilhouetteFrame(width: Int, height: Int, drawFunc: (Pixmap) -> Unit): Texture {
        val pixmap = Pixmap(width, height, Pixmap.Format.RGBA8888)
        pixmap.setColor(0.05f, 0.05f, 0.1f, 1f) // Dark silhouette color
        drawFunc(pixmap)
        val texture = Texture(pixmap)
        pixmap.dispose()
        return texture
    }

    // Drawing helper functions for different poses
    private fun drawStandingPose(pixmap: Pixmap, breathOffset: Float) {
        val cx = pixmap.width / 2
        val groundY = 10

        // Head
        pixmap.fillCircle(cx, pixmap.height - 30, 20)

        // Neck
        pixmap.fillRectangle(cx - 6, pixmap.height - 55, 12, 15)

        // Torso
        drawTorso(pixmap, cx, pixmap.height - 70, breathOffset.toInt())

        // Arms in relaxed position
        drawArm(pixmap, cx - 30, pixmap.height - 75, -10f, 80f)
        drawArm(pixmap, cx + 30, pixmap.height - 75, 10f, 80f)

        // Legs
        drawLeg(pixmap, cx - 15, pixmap.height - 130, -5f, 100f)
        drawLeg(pixmap, cx + 15, pixmap.height - 130, 5f, 100f)
    }

    private fun drawWalkingPose(pixmap: Pixmap, legOffset: Float, armOffset: Float) {
        val cx = pixmap.width / 2
        val groundY = 10

        // Head (slight bob)
        val headBob = (Math.abs(legOffset) / 25f * 3).toInt()
        pixmap.fillCircle(cx + 5, pixmap.height - 30 + headBob, 20)

        // Neck
        pixmap.fillRectangle(cx - 4, pixmap.height - 55 + headBob, 12, 15)

        // Torso (slight lean forward)
        drawTorso(pixmap, cx + 3, pixmap.height - 70 + headBob, 0)

        // Arms swinging opposite to legs
        drawArm(pixmap, cx - 25, pixmap.height - 75, -20f + armOffset, 70f)
        drawArm(pixmap, cx + 25, pixmap.height - 75, 20f - armOffset, 70f)

        // Legs walking
        drawLeg(pixmap, cx - 12, pixmap.height - 130, legOffset, 100f)
        drawLeg(pixmap, cx + 12, pixmap.height - 130, -legOffset, 100f)
    }

    private fun drawJumpPose(pixmap: Pixmap, phase: Float) {
        val cx = pixmap.width / 2

        // Tucked position in air
        val tuck = if (phase > 0.3f && phase < 0.7f) 20f else 0f

        // Head
        pixmap.fillCircle(cx, pixmap.height - 30 - tuck.toInt(), 20)

        // Neck
        pixmap.fillRectangle(cx - 6, pixmap.height - 55 - tuck.toInt(), 12, 15)

        // Torso
        drawTorso(pixmap, cx, pixmap.height - 70 - tuck.toInt(), 0)

        // Arms raised
        drawArm(pixmap, cx - 30, pixmap.height - 75 - tuck.toInt(), -60f, 65f)
        drawArm(pixmap, cx + 30, pixmap.height - 75 - tuck.toInt(), 60f, 65f)

        // Legs tucked
        val legAngle = tuck * 1.5f
        drawLeg(pixmap, cx - 15, pixmap.height - 130 - tuck.toInt(), -20f - legAngle, 90f)
        drawLeg(pixmap, cx + 15, pixmap.height - 130 - tuck.toInt(), 20f + legAngle, 90f)
    }

    private fun drawPunchPose(pixmap: Pixmap, phase: Float) {
        val cx = pixmap.width / 2

        // Head
        pixmap.fillCircle(cx + (phase * 10).toInt(), pixmap.height - 30, 20)

        // Neck
        pixmap.fillRectangle(cx - 4 + (phase * 8).toInt(), pixmap.height - 55, 12, 15)

        // Torso rotated
        drawTorso(pixmap, cx + (phase * 15).toInt(), pixmap.height - 70, 0)

        // Punching arm extends
        val punchExtension = if (phase < 0.5f) phase * 2 else 2f - phase * 2
        drawArm(pixmap, cx + 20 + (punchExtension * 50).toInt(), pixmap.height - 70, 85f, 60f + punchExtension * 30f)

        // Back arm
        drawArm(pixmap, cx - 25, pixmap.height - 75, -30f, 60f)

        // Legs in fighting stance
        drawLeg(pixmap, cx - 20, pixmap.height - 130, -15f, 100f)
        drawLeg(pixmap, cx + 10, pixmap.height - 130, 25f, 100f)
    }

    private fun drawKickPose(pixmap: Pixmap, phase: Float) {
        val cx = pixmap.width / 2

        // Lean back during kick
        val leanBack = if (phase > 0.2f && phase < 0.8f) 15 else 0

        // Head
        pixmap.fillCircle(cx - leanBack, pixmap.height - 30, 20)

        // Neck
        pixmap.fillRectangle(cx - 6 - leanBack, pixmap.height - 55, 12, 15)

        // Torso leaning back
        drawTorso(pixmap, cx - leanBack, pixmap.height - 70, 0)

        // Arms for balance
        drawArm(pixmap, cx - 30 - leanBack, pixmap.height - 75, -40f, 65f)
        drawArm(pixmap, cx + 20 - leanBack, pixmap.height - 75, 30f, 65f)

        // Kicking leg
        val kickAngle = if (phase < 0.5f) phase * 180f else (1f - phase) * 180f
        drawLeg(pixmap, cx + (phase * 60).toInt(), pixmap.height - 120, 70f + kickAngle * 0.3f, 110f)

        // Standing leg
        drawLeg(pixmap, cx - 15 - leanBack, pixmap.height - 130, -10f, 100f)
    }

    private fun drawHighKickPose(pixmap: Pixmap, phase: Float) {
        val cx = pixmap.width / 2

        val kickHeight = if (phase < 0.4f) phase * 2.5f else if (phase < 0.6f) 1f else (1f - phase) * 2.5f

        // Head
        pixmap.fillCircle(cx - 10, pixmap.height - 30, 20)

        // Neck
        pixmap.fillRectangle(cx - 14, pixmap.height - 55, 12, 15)

        // Torso
        drawTorso(pixmap, cx - 10, pixmap.height - 70, 0)

        // Arms
        drawArm(pixmap, cx - 35, pixmap.height - 75, -50f, 65f)
        drawArm(pixmap, cx + 15, pixmap.height - 75, 40f, 65f)

        // High kicking leg
        val kickY = (kickHeight * 100).toInt()
        drawLeg(pixmap, cx + 20, pixmap.height - 80 + kickY, 80f, 100f)

        // Standing leg
        drawLeg(pixmap, cx - 20, pixmap.height - 130, -5f, 100f)
    }

    private fun drawBlockPose(pixmap: Pixmap) {
        val cx = pixmap.width / 2

        // Head
        pixmap.fillCircle(cx, pixmap.height - 35, 20)

        // Neck
        pixmap.fillRectangle(cx - 6, pixmap.height - 60, 12, 15)

        // Torso slightly crouched
        drawTorso(pixmap, cx, pixmap.height - 75, 0)

        // Arms in guard position
        drawArm(pixmap, cx - 15, pixmap.height - 70, -80f, 55f)
        drawArm(pixmap, cx + 15, pixmap.height - 70, 80f, 55f)

        // Legs in defensive stance
        drawLeg(pixmap, cx - 18, pixmap.height - 135, -20f, 95f)
        drawLeg(pixmap, cx + 18, pixmap.height - 135, 20f, 95f)
    }

    private fun drawHitStunPose(pixmap: Pixmap, phase: Float) {
        val cx = pixmap.width / 2
        val recoil = (phase * 30).toInt()

        // Head thrown back
        pixmap.fillCircle(cx - recoil, pixmap.height - 25 - (phase * 10).toInt(), 20)

        // Neck
        pixmap.fillRectangle(cx - 8 - recoil, pixmap.height - 50, 12, 15)

        // Torso leaning back
        drawTorso(pixmap, cx - recoil / 2, pixmap.height - 65, 0)

        // Arms flailing
        drawArm(pixmap, cx - 20 - recoil, pixmap.height - 70, -30f - phase * 40f, 65f)
        drawArm(pixmap, cx + 25 - recoil, pixmap.height - 70, 45f + phase * 30f, 65f)

        // Legs stumbling
        drawLeg(pixmap, cx - 15 - recoil / 2, pixmap.height - 125, -10f - phase * 15f, 95f)
        drawLeg(pixmap, cx + 20 - recoil / 2, pixmap.height - 125, 15f + phase * 20f, 95f)
    }

    private fun drawCrouchPose(pixmap: Pixmap) {
        val cx = pixmap.width / 2

        // Head lower
        pixmap.fillCircle(cx, pixmap.height - 60, 20)

        // Neck
        pixmap.fillRectangle(cx - 6, pixmap.height - 85, 12, 15)

        // Torso compressed
        drawTorso(pixmap, cx, pixmap.height - 95, -10)

        // Arms ready
        drawArm(pixmap, cx - 25, pixmap.height - 95, -20f, 50f)
        drawArm(pixmap, cx + 25, pixmap.height - 95, 20f, 50f)

        // Legs crouched
        drawLeg(pixmap, cx - 20, pixmap.height - 130, -45f, 70f)
        drawLeg(pixmap, cx + 20, pixmap.height - 130, 45f, 70f)
    }

    private fun drawTorso(pixmap: Pixmap, cx: Int, top: Int, squeeze: Int) {
        // Draw torso as trapezoid
        val torsoHeight = 60 + squeeze
        for (y in 0 until torsoHeight) {
            val progress = y.toFloat() / torsoHeight
            val halfWidth = (18 + progress * 12).toInt()
            pixmap.fillRectangle(cx - halfWidth, top - y, halfWidth * 2, 1)
        }
    }

    private fun drawArm(pixmap: Pixmap, startX: Int, startY: Int, angle: Float, length: Float) {
        val radians = Math.toRadians(angle.toDouble())
        val endX = startX + (cos(radians) * length).toInt()
        val endY = startY - (sin(radians) * length).toInt()

        // Draw arm as thick line (multiple lines for thickness)
        for (offset in -4..4) {
            pixmap.drawLine(startX, startY + offset, endX, endY + offset)
        }

        // Hand
        pixmap.fillCircle(endX, endY, 8)
    }

    private fun drawLeg(pixmap: Pixmap, startX: Int, startY: Int, angle: Float, length: Float) {
        val radians = Math.toRadians((270 + angle).toDouble())
        val kneeLength = length * 0.5f
        val kneeX = startX + (cos(radians) * kneeLength).toInt()
        val kneeY = startY - (sin(radians) * kneeLength).toInt()

        // Upper leg
        for (offset in -5..5) {
            pixmap.drawLine(startX + offset, startY, kneeX + offset, kneeY)
        }

        // Lower leg (more vertical)
        val footX = kneeX + (angle * 0.3f).toInt()
        val footY = 15

        for (offset in -4..4) {
            pixmap.drawLine(kneeX + offset, kneeY, footX + offset, footY)
        }

        // Foot
        pixmap.fillRectangle(footX - 8, footY - 5, 20, 10)
    }

    /**
     * Set current animation
     */
    fun setAnimation(type: AnimationType, resetTime: Boolean = true) {
        if (currentAnimation != type || resetTime) {
            currentAnimation = type
            if (resetTime) stateTime = 0f
            isAnimationFinished = false
        }
    }

    /**
     * Update animation state
     */
    fun update(deltaTime: Float) {
        stateTime += deltaTime

        animations[currentAnimation]?.let { animation ->
            isAnimationFinished = animation.isAnimationFinished(stateTime)
        }
    }

    /**
     * Get current frame
     */
    fun getCurrentFrame(): TextureRegion? {
        return animations[currentAnimation]?.getKeyFrame(stateTime)
    }

    override fun dispose() {
        generatedTextures.forEach { it.dispose() }
        generatedTextures.clear()
        animations.clear()
    }
}

enum class AnimationType {
    IDLE,
    WALK,
    RUN,
    JUMP,
    FALL,
    CROUCH,
    PUNCH,
    KICK,
    HIGH_KICK,
    WEAPON_SLASH,
    BLOCK,
    HIT_STUN,
    KNOCKDOWN,
    GET_UP,
    VICTORY,
    DEFEAT
}
