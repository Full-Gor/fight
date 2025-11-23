package com.shadowfight.game.core

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.badlogic.gdx.Gdx

/**
 * Android Firebase implementation using actual Firebase SDK
 */
class AndroidFirebaseService(private val context: Context) : FirebaseService {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var analytics: FirebaseAnalytics

    override fun initialize() {
        try {
            FirebaseApp.initializeApp(context)
            auth = Firebase.auth
            firestore = Firebase.firestore
            analytics = Firebase.analytics
            Gdx.app.log("Firebase", "Firebase initialized successfully")
        } catch (e: Exception) {
            Gdx.app.error("Firebase", "Failed to initialize Firebase: ${e.message}")
        }
    }

    override fun signInAnonymously(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        auth.signInAnonymously()
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: ""
                Gdx.app.log("Firebase", "Anonymous sign-in successful: $userId")
                onSuccess(userId)
            }
            .addOnFailureListener { exception ->
                Gdx.app.error("Firebase", "Anonymous sign-in failed: ${exception.message}")
                onError(exception.message ?: "Unknown error")
            }
    }

    override fun signInWithEmail(email: String, password: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: ""
                Gdx.app.log("Firebase", "Email sign-in successful: $userId")
                onSuccess(userId)
            }
            .addOnFailureListener { exception ->
                Gdx.app.error("Firebase", "Email sign-in failed: ${exception.message}")
                onError(exception.message ?: "Unknown error")
            }
    }

    override fun signUp(email: String, password: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: ""
                Gdx.app.log("Firebase", "Sign-up successful: $userId")
                onSuccess(userId)
            }
            .addOnFailureListener { exception ->
                Gdx.app.error("Firebase", "Sign-up failed: ${exception.message}")
                onError(exception.message ?: "Unknown error")
            }
    }

    override fun signOut() {
        auth.signOut()
        Gdx.app.log("Firebase", "Signed out")
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun isSignedIn(): Boolean = auth.currentUser != null

    override fun savePlayerData(userId: String, data: Map<String, Any>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firestore.collection("players")
            .document(userId)
            .set(data)
            .addOnSuccessListener {
                Gdx.app.log("Firebase", "Player data saved for $userId")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Gdx.app.error("Firebase", "Failed to save player data: ${exception.message}")
                onError(exception.message ?: "Unknown error")
            }
    }

    override fun loadPlayerData(userId: String, onSuccess: (Map<String, Any>?) -> Unit, onError: (String) -> Unit) {
        firestore.collection("players")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val data = document.data
                Gdx.app.log("Firebase", "Player data loaded for $userId")
                onSuccess(data)
            }
            .addOnFailureListener { exception ->
                Gdx.app.error("Firebase", "Failed to load player data: ${exception.message}")
                onError(exception.message ?: "Unknown error")
            }
    }

    override fun saveHighScore(userId: String, score: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val scoreData = mapOf(
            "userId" to userId,
            "score" to score,
            "timestamp" to System.currentTimeMillis(),
            "displayName" to (auth.currentUser?.displayName ?: "Player")
        )

        firestore.collection("leaderboard")
            .document(userId)
            .set(scoreData)
            .addOnSuccessListener {
                Gdx.app.log("Firebase", "High score saved: $score")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Unknown error")
            }
    }

    override fun getLeaderboard(limit: Int, onSuccess: (List<LeaderboardEntry>) -> Unit, onError: (String) -> Unit) {
        firestore.collection("leaderboard")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .addOnSuccessListener { documents ->
                val entries = documents.mapIndexed { index, doc ->
                    LeaderboardEntry(
                        oderId = doc.id,
                        displayName = doc.getString("displayName") ?: "Unknown",
                        score = doc.getLong("score")?.toInt() ?: 0,
                        rank = index + 1
                    )
                }
                onSuccess(entries)
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Unknown error")
            }
    }

    override fun logEvent(eventName: String, params: Map<String, Any>) {
        val bundle = android.os.Bundle()
        params.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putBoolean(key, value)
            }
        }
        analytics.logEvent(eventName, bundle)
    }

    override fun setUserProperty(name: String, value: String) {
        analytics.setUserProperty(name, value)
    }
}
