package com.example.synclog.document.service

import com.example.synclog.common.exception.WorkspaceNotFoundException
import com.example.synclog.document.controller.DocumentSimpleResponse
import com.example.synclog.document.persistence.Document
import com.example.synclog.document.persistence.DocumentRepository
import com.example.synclog.workspace.persistence.WorkspaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val workspaceRepository: WorkspaceRepository,
) {
    @Transactional
    fun createDocument(workspaceId: Long): DocumentSimpleResponse {
        val workspace = workspaceRepository.findById(workspaceId).orElseThrow { WorkspaceNotFoundException() }
        val document =
            documentRepository.save(
                Document(
                    title = "새 문서",
                    workspace = workspace,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                ),
            )
        return DocumentSimpleResponse.fromEntity(document)
    }
}
