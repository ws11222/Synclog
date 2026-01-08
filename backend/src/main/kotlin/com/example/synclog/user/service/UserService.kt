package com.example.synclog.user.service

import com.example.synclog.common.exception.LoginInvalidPasswordException
import com.example.synclog.common.exception.SignUpEmailConflictException
import com.example.synclog.common.exception.UserNotFoundException
import com.example.synclog.common.security.JwtProvider
import com.example.synclog.user.controller.AuthRequest
import com.example.synclog.user.controller.AuthResponse
import com.example.synclog.user.persistence.User
import com.example.synclog.user.persistence.UserRepository
import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository,
) {
    @Transactional
    fun signUp(requestDto: AuthRequest): AuthResponse {
        if (userRepository.existsByEmail(requestDto.email)) throw SignUpEmailConflictException()
        val encryptedPassword = BCrypt.hashpw(requestDto.password, BCrypt.gensalt())
        val user =
            userRepository.save(
                User(
                    email = requestDto.email,
                    password = encryptedPassword,
                ),
            )

        val accessToken = jwtProvider.generateToken(user.id!!)
        return AuthResponse(accessToken)
    }

    @Transactional
    fun login(requestDto: AuthRequest): AuthResponse {
        val user = userRepository.findByEmail(requestDto.email) ?: throw UserNotFoundException()
        if (!BCrypt.checkpw(requestDto.password, user.password)) throw LoginInvalidPasswordException()
        val accessToken = jwtProvider.generateToken(requestDto.email)
        return AuthResponse(accessToken)
    }
}
