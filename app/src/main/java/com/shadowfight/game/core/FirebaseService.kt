package com.shadowfight.game.core

import com.badlogic.gdx.Gdx

/**
 * Firebase service wrapper for cross-platform compatibility
 * On Android: Uses actual Firebase SDK
 * On Desktop: Uses mock/REST API fallback
 */
interface FirebaseService {
    fun initialize()
    fun signInAnonymously(onSuccess: (String) -> Unit, onError: (String) -> Unit)
    fun signInWithEmail(email: String, password: String, onSuccess: (String) -> Unit, onError: (String) -> Unit)
    fun signUp(email: String, password: String, onSuccess: (String) -> Unit, onError: (String) -> Unit)
    fun signOut()
    fun getCurrentUserId(): String?
    fun isSignedIn(): Boolean

    // Firestore operations
    fun savePlayerData(userId: String, data: Map<String, Any>, onSuccess: () -> Unit, onError: (String) -> Unit)
    fun loadPlayerData(userId: String, onSuccess: (Map<String, Any>?) -> Unit, onError: (String) -> Unit)
    fun saveHighScore(userId: String, score: Int, onSuccess: () -> Unit, onError: (String) -> Unit)
    fun getLeaderboard(limit: Int, onSuccess: (List<LeaderboardEntry>) -> Unit, onError: (String) -> Unit)

    // Analytics
    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap())
    fun setUserProperty(name: String, value: String)
}

data class LeaderboardEntry(
    val oderId: String,
    val displayName: String,
    val score: Int,
    val rank: Int
)

/**
 * Desktop implementation - uses local storage as fallback
 */
class DesktopFirebaseService : FirebaseService {
    private var currentUserId: String? = null
    private val localStorage = mutableMapOf<String, Map<String, Any>>()

    override fun initialize() {
        Gdx.app.log("Firebase", "Desktop Firebase initialized (mock mode)")
    }

    override fun signInAnonymously(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        currentUserId = "desktop_user_${System.currentTimeMillis()}"
        Gdx.app.log("Firebase", "Anonymous sign-in: $currentUserId")
        onSuccess(currentUserId!!)
    }

    override fun signInWithEmail(email: String, password: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        // Mock implementation
        currentUserId = "user_${email.hashCode()}"
        onSuccess(currentUserId!!)
    }

    override fun signUp(email: String, password: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        currentUserId = "user_${email.hashCode()}"
        onSuccess(currentUserId!!)
    }

    override fun signOut() {
        currentUserId = null
    }

    override fun getCurrentUserId(): String? = currentUserId

    override fun isSignedIn(): Boolean = currentUserId != null

    override fun savePlayerData(userId: String, data: Map<String, Any>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        localStorage[userId] = data
        Gdx.app.log("Firebase", "Saved player data for $userId")
        onSuccess()
    }

    override fun loadPlayerData(userId: String, onSuccess: (Map<String, Any>?) -> Unit, onError: (String) -> Unit) {
        val data = localStorage[userId]
        onSuccess(data)
    }

    override fun saveHighScore(userId: String, score: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        Gdx.app.log("Firebase", "Saved high score: $score for $userId")
        onSuccess()
    }

    override fun getLeaderboard(limit: Int, onSuccess: (List<LeaderboardEntry>) -> Unit, onError: (String) -> Unit) {
        // Return mock leaderboard
        val mockLeaderboard = listOf(
            LeaderboardEntry("1", "ShadowMaster", 99999, 1),
            LeaderboardEntry("2", "NinjaWarrior", 85000, 2),
            LeaderboardEntry("3", "DemonSlayer", 72000, 3),
            LeaderboardEntry("4", "Player", 50000, 4),
            LeaderboardEntry("5", "Fighter01", 45000, 5)
        )
        onSuccess(mockLeaderboard.take(limit))
    }

    override fun logEvent(eventName: String, params: Map<String, Any>) {
        Gdx.app.log("Analytics", "Event: $eventName, params: $params")
    }

    override fun setUserProperty(name: String, value: String) {
        Gdx.app.log("Analytics", "User property: $name = $value")
    }
}

/**
 * Factory to create appropriate Firebase service based on platform
 */
object FirebaseServiceFactory {
    fun create(): FirebaseService {
        return when (Gdx.app.type) {
            com.badlogic.gdx.Application.ApplicationType.Android -> {
                // On Android, we'll use the actual Firebase implementation
                // This would be set via dependency injection in the Android launcher
                DesktopFirebaseService() // Placeholder - will be replaced
            }
            else -> DesktopFirebaseService()
        }
    }
}
