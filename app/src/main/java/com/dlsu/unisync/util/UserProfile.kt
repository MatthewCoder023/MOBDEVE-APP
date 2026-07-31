package com.dlsu.unisync.util

import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

// Presentation helpers for the signed-in Firebase user. Email/password accounts
// have no display name, so we derive one from the email local part.
object UserProfile {
    private const val FALLBACK_NAME = "Lasallian"

    fun displayName(): String {
        val user = FirebaseAuth.getInstance().currentUser ?: return FALLBACK_NAME
        user.displayName?.takeIf { it.isNotBlank() }?.let { return it }
        val local = user.email?.substringBefore('@').orEmpty()
        if (local.isBlank()) return FALLBACK_NAME
        return local.split('.', '_', '-')
            .filter { it.isNotBlank() }
            .joinToString(" ") { part ->
                part.replaceFirstChar { it.titlecase(Locale.getDefault()) }
            }
    }

    fun firstName(): String = displayName().substringBefore(' ')

    fun email(): String = FirebaseAuth.getInstance().currentUser?.email.orEmpty()

    fun initials(): String = displayName()
        .split(' ')
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
        .ifEmpty { "U" }
}
