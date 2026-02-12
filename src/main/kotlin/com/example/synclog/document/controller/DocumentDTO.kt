package com.example.synclog.document.controller

import com.example.synclog.document.persistence.Document
import java.time.LocalDateTime

data class DocumentSimpleResponse(
    val id: Long,
    val title: String,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun fromEntity(entity: Document): DocumentSimpleResponse {
            return DocumentSimpleResponse(
                id = entity.id!!,
                title = entity.title,
                updatedAt = entity.updatedAt,
            )
        }
    }
}

data class DocumentMetadataResponse(
    val documentId: Long,
    val title: String,
    val workspaceName: String,
)

data class DocumentTitleRequest(
    val title: String,
)

data class DocumentSnapshotRequest(
    val plainText: String,
    val fullBinary: String,
)
