package com.example.synclog.user.controller

import com.example.synclog.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class UserController(
    private val userService: UserService,
) {
    @PostMapping("/auth/signup")
    fun signUp(
        @RequestBody requestDto: SignupRequest,
    ): ResponseEntity<AuthResponse> {
        val response = userService.signUp(requestDto)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/auth/login")
    fun login(
        @RequestBody requestDto: LoginRequest,
    ): ResponseEntity<AuthResponse> {
        val response = userService.login(requestDto)
        return ResponseEntity.ok(response)
    }
}
