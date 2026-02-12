package com.example.synclog.user.controller

data class LoginRequest(
    val email: String,
    val password: String,
)

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
)

data class AuthResponse(
    val accessToken: String,
    val name: String,
)
