package com.example.synclog.common.exception

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class WorkspaceException(
    code: HttpStatusCode,
    message: String,
) : DomainException(code, message)

class WorkspaceNotFoundException : WorkspaceException(
    code = HttpStatus.NOT_FOUND,
    message = "Workspace not found",
)

class MemberNotFoundException : WorkspaceException(
    code = HttpStatus.NOT_FOUND,
    message = "user not belongs to workspace",
)

class NotEnoughRoleException : WorkspaceException(
    code = HttpStatus.FORBIDDEN,
    message = "This user doesn't have enough role",
)
