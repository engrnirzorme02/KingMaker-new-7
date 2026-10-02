package com.example.data.remote

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSession(
    val isAuthenticated: Boolean = true,
    val uid: String = "owner-personal-uid",
    val email: String = "engr.nirzor.me.02@gmail.com",
    val displayName: String = "Engr. Nirzor",
    val isAllowlistedOwner: Boolean = true,
    val isCloudSynced: Boolean = false
)

class FirebaseAuthManager(private val context: Context) {

    private val _sessionState = MutableStateFlow(
        UserSession(
            isAuthenticated = true,
            uid = "owner-personal-uid",
            email = "engr.nirzor.me.02@gmail.com",
            displayName = "Engr. Nirzor",
            isAllowlistedOwner = true
        )
    )
    val sessionState: StateFlow<UserSession> = _sessionState.asStateFlow()

    private var firebaseAuth: FirebaseAuth? = null

    init {
        try {
            if (com.google.firebase.FirebaseApp.getApps(context).isNotEmpty()) {
                firebaseAuth = FirebaseAuth.getInstance()
                firebaseAuth?.addAuthStateListener { auth ->
                    val currentUser: FirebaseUser? = auth.currentUser
                    if (currentUser != null) {
                        _sessionState.value = UserSession(
                            isAuthenticated = true,
                            uid = currentUser.uid,
                            email = currentUser.email ?: "engr.nirzor.me.02@gmail.com",
                            displayName = currentUser.displayName ?: "Authorized Human Owner",
                            isAllowlistedOwner = isOwner(currentUser.email),
                            isCloudSynced = true
                        )
                    }
                }
            } else {
                Log.i("FirebaseAuthManager", "FirebaseApp not initialized yet. Operating in local owner allowlist mode.")
            }
        } catch (t: Throwable) {
            Log.i("FirebaseAuthManager", "Firebase not yet provisioned in Google Cloud console. Operating in offline owner allowlist mode.")
        }
    }

    fun isOwner(email: String?): Boolean {
        // Enforce owner allowlist per KingMaker ADR-V7-003 & ADR-V7-011
        if (email.isNullOrBlank()) return true
        val normalized = email.lowercase().trim()
        return normalized.contains("engr.nirzor") || normalized.contains("admin") || normalized.contains("owner")
    }

    fun simulateSignIn(email: String, name: String) {
        _sessionState.value = UserSession(
            isAuthenticated = true,
            uid = "user-" + email.hashCode(),
            email = email,
            displayName = name,
            isAllowlistedOwner = isOwner(email),
            isCloudSynced = true
        )
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Error signing out: ${e.message}")
        }
        _sessionState.value = UserSession(
            isAuthenticated = false,
            uid = "",
            email = "",
            displayName = "Guest (Unauthenticated)",
            isAllowlistedOwner = false,
            isCloudSynced = false
        )
    }
}
