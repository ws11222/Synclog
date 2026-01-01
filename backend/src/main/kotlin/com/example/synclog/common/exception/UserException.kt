package com.example.synclog.common.exception

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class UserException(
    code: HttpStatusCode,
    message: String,
) : DomainException(code, message)

class AuthenticateException : UserException(
    code = HttpStatus.UNAUTHORIZED,
    message = "Authenticate failed",
)

class SignUpEmailConflictException : UserException(
    code = HttpStatus.CONFLICT,
    message = "Email conflict",
)

class UserNotFoundException : UserException(
    code = HttpStatus.NOT_FOUND,
    message = "User not found",
)

class LoginInvalidPasswordException : UserException(
    code = HttpStatus.BAD_REQUEST,
    message = "Invalid password",
)
