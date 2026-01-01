package com.example.synclog.user.controller

data class AuthRequest(
    val email: String,
    val password: String,
)

data class AuthResponse(
    val accessToken: String,
)
