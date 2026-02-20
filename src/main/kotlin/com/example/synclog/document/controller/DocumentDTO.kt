package com.example.synclog.document.controller

import com.example.synclog.document.persistence.Document
import java.time.LocalDate
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
    val fullBinary: String?,
)

data class DocumentTitleRequest(
    val title: String,
)

data class DocumentSnapshotRequest(
    val plainText: String,
    val fullBinary: String,
)

data class DocumentRagRequest(
    val request: String,
)

data class DocumentRagResponse(
    val response: String,
)

data class DocumentTaskResponse(
    val response: List<SingleTask>,
)

data class SingleTask(
    val title: String,
    val name: String?,
    val date: LocalDate?,
)
