package com.example.synclog.common.exception

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class DocumentException(
    code: HttpStatusCode,
    message: String,
) : DomainException(code, message)

class DocumentNotFoundException : DocumentException(
    code = HttpStatus.NOT_FOUND,
    message = "Document not found",
)
