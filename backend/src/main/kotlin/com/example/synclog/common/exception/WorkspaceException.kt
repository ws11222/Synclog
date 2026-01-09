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
