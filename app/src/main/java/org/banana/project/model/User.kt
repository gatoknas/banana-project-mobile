package org.banana.project.model

import androidx.annotation.Keep
import androidx.compose.runtime.Stable

@Stable
@Keep
data class User(
    val id: String,
    val username: String,
    val name: String,
    val role: String
) {
    val avatar: String get() = "https://api.dicebear.com/7.x/pixel-art/svg?seed=$username"
}
