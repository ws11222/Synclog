package com.example.synclog.workspace.controller

import com.example.synclog.document.controller.DocumentSimpleResponse
import com.example.synclog.workspace.persistence.Workspace

data class CreateWorkspaceRequest(
    val title: String,
)

data class WorkspaceResponse(
    val id: Long,
    val title: String,
    val documentCount: Int,
    val memberCount: Int,
    val documents: List<DocumentSimpleResponse>,
) {
    companion object {
        fun fromEntity(entity: Workspace): WorkspaceResponse {
            return WorkspaceResponse(
                id = entity.id!!,
                title = entity.title,
                documentCount = entity.documents.size,
                memberCount = entity.members.size,
                documents = entity.documents.map { DocumentSimpleResponse.fromEntity(it) },
            )
        }
    }
}
