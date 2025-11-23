package com.shadowfight.game.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Disposable

/**
 * Manages all game audio - music and sound effects
 */
class AudioManager : Disposable {

    // Sound effects
    private val sounds = mutableMapOf<SoundType, Sound?>()

    // Music tracks
    private val musicTracks = mutableMapOf<MusicType, Music?>()
    private var currentMusic: Music? = null
    private var currentMusicType: MusicType? = null

    // Volume settings
    var masterVolume: Float = 1.0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            updateMusicVolume()
        }

    var musicVolume: Float = 0.7f
        set(value) {
            field = value.coerceIn(0f, 1f)
            updateMusicVolume()
        }

    var sfxVolume: Float = 1.0f
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    var isMuted: Boolean = false
        set(value) {
            field = value
            if (value) {
                currentMusic?.volume = 0f
            } else {
                updateMusicVolume()
            }
        }

    /**
     * Initialize audio system and load sounds
     */
    fun initialize() {
        Gdx.app.log("AudioManager", "Initializing audio system...")

        // In a real game, we would load actual audio files here
        // For now, we'll create placeholder references

        // The sound loading would look like this:
        // sounds[SoundType.PUNCH_HIT] = Gdx.audio.newSound(Gdx.files.internal("sounds/punch_hit.wav"))

        Gdx.app.log("AudioManager", "Audio system initialized")
    }

    /**
     * Play a sound effect
     */
    fun playSound(type: SoundType, volume: Float = 1f, pitch: Float = 1f, pan: Float = 0f): Long {
        if (isMuted) return -1

        val sound = sounds[type]
        return sound?.play(sfxVolume * masterVolume * volume, pitch, pan) ?: -1
    }

    /**
     * Play combat sound based on attack type
     */
    fun playCombatSound(isHit: Boolean, isBlocked: Boolean = false, isCritical: Boolean = false) {
        when {
            isBlocked -> playSound(SoundType.BLOCK)
            isCritical -> playSound(SoundType.CRITICAL_HIT, pitch = 0.9f)
            isHit -> {
                // Randomize hit sounds
                val hitSounds = listOf(SoundType.PUNCH_HIT, SoundType.KICK_HIT)
                playSound(hitSounds.random())
            }
            else -> {
                // Whiff sound
                val whiffSounds = listOf(SoundType.PUNCH_WHIFF, SoundType.KICK_WHIFF)
                playSound(whiffSounds.random(), volume = 0.5f)
            }
        }
    }

    /**
     * Play UI sound
     */
    fun playUISound(type: UISound) {
        val soundType = when (type) {
            UISound.BUTTON_CLICK -> SoundType.UI_CLICK
            UISound.MENU_SELECT -> SoundType.UI_SELECT
            UISound.MENU_BACK -> SoundType.UI_BACK
            UISound.PURCHASE -> SoundType.UI_PURCHASE
            UISound.ERROR -> SoundType.UI_ERROR
            UISound.VICTORY -> SoundType.VICTORY
            UISound.DEFEAT -> SoundType.DEFEAT
            UISound.ROUND_START -> SoundType.ROUND_START
        }
        playSound(soundType)
    }

    /**
     * Play background music
     */
    fun playMusic(type: MusicType, loop: Boolean = true, fadeIn: Boolean = true) {
        if (currentMusicType == type && currentMusic?.isPlaying == true) return

        // Stop current music
        currentMusic?.stop()

        // Get or load music track
        var music = musicTracks[type]
        if (music == null) {
            // In a real game, we would load the music file here
            // music = Gdx.audio.newMusic(Gdx.files.internal("music/${type.fileName}"))
            // musicTracks[type] = music
        }

        currentMusic = music
        currentMusicType = type

        music?.apply {
            isLooping = loop
            volume = if (isMuted) 0f else musicVolume * masterVolume
            play()
        }
    }

    /**
     * Stop current music
     */
    fun stopMusic(fadeOut: Boolean = false) {
        currentMusic?.stop()
        currentMusic = null
        currentMusicType = null
    }

    /**
     * Pause current music
     */
    fun pauseMusic() {
        currentMusic?.pause()
    }

    /**
     * Resume paused music
     */
    fun resumeMusic() {
        currentMusic?.play()
    }

    private fun updateMusicVolume() {
        currentMusic?.volume = if (isMuted) 0f else musicVolume * masterVolume
    }

    override fun dispose() {
        sounds.values.filterNotNull().forEach { it.dispose() }
        musicTracks.values.filterNotNull().forEach { it.dispose() }
        sounds.clear()
        musicTracks.clear()
    }
}

/**
 * Types of sound effects
 */
enum class SoundType {
    // Combat sounds
    PUNCH_HIT,
    PUNCH_WHIFF,
    KICK_HIT,
    KICK_WHIFF,
    WEAPON_HIT,
    WEAPON_WHIFF,
    BLOCK,
    CRITICAL_HIT,
    KNOCKDOWN,
    GET_UP,

    // Character sounds
    JUMP,
    LAND,
    DASH,
    GRUNT_MALE,
    GRUNT_FEMALE,
    PAIN_MALE,
    PAIN_FEMALE,

    // Magic/Special
    MAGIC_FIRE,
    MAGIC_ICE,
    MAGIC_LIGHTNING,
    MAGIC_SHADOW,
    RANGED_THROW,
    RANGED_HIT,

    // UI sounds
    UI_CLICK,
    UI_SELECT,
    UI_BACK,
    UI_PURCHASE,
    UI_ERROR,

    // Game events
    ROUND_START,
    ROUND_END,
    VICTORY,
    DEFEAT,
    LEVEL_UP,
    UNLOCK
}

/**
 * Types of background music
 */
enum class MusicType(val fileName: String) {
    MENU("menu_theme.ogg"),
    FIGHT_ACT1("fight_hermit.ogg"),
    FIGHT_ACT2("fight_butcher.ogg"),
    FIGHT_ACT3("fight_wasp.ogg"),
    FIGHT_ACT4("fight_widow.ogg"),
    FIGHT_ACT5("fight_shogun.ogg"),
    FIGHT_ACT6("fight_titan.ogg"),
    FIGHT_BOSS("boss_battle.ogg"),
    VICTORY("victory.ogg"),
    DEFEAT("defeat.ogg"),
    SHOP("shop_theme.ogg")
}

/**
 * UI sound categories
 */
enum class UISound {
    BUTTON_CLICK,
    MENU_SELECT,
    MENU_BACK,
    PURCHASE,
    ERROR,
    VICTORY,
    DEFEAT,
    ROUND_START
}
