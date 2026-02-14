package com.example.synclog.document.service

import com.example.synclog.common.exception.DocumentNotFoundException
import com.example.synclog.common.exception.WorkspaceNotFoundException
import com.example.synclog.document.controller.DocumentMetadataResponse
import com.example.synclog.document.controller.DocumentSimpleResponse
import com.example.synclog.document.controller.DocumentTitleRequest
import com.example.synclog.document.persistence.Document
import com.example.synclog.document.persistence.DocumentContent
import com.example.synclog.document.persistence.DocumentContentRepository
import com.example.synclog.document.persistence.DocumentRepository
import com.example.synclog.workspace.persistence.WorkspaceRepository
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val documentContentRepository: DocumentContentRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val embeddingModel: EmbeddingModel,
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

        val newContent = DocumentContent(document = document, plainText = "", yjsBinary = ByteArray(0), embedding = null)
        document.content = newContent
        documentContentRepository.save(newContent)
        return DocumentSimpleResponse.fromEntity(document)
    }

    @Transactional
    fun saveFullSnapshot(
        docId: Long,
        text: String,
        fullBinary: ByteArray,
    ) {
        val document = documentRepository.findById(docId).orElseThrow { DocumentNotFoundException() }
        document.updatedAt = LocalDateTime.now()
        val content =
            documentContentRepository.findById(docId)
                .orElseGet {
                    val newContent = DocumentContent(document = document)
                    document.content = newContent
                    newContent
                }

        val isTextChanged = content.plainText != text

        content.plainText = text
        content.yjsBinary = fullBinary

        // 변화가 생기면 plainText를 embedding으로 변환하여 저장
        if (isTextChanged && text.isNotBlank()) {
            content.embedding = embeddingModel.embed(text)
        }
        documentContentRepository.save(content)
    }

    @Transactional
    fun getMetadata(documentId: Long): DocumentMetadataResponse {
        val document = documentRepository.findById(documentId).orElseThrow { DocumentNotFoundException() }
        return DocumentMetadataResponse(
            documentId = document.id!!,
            title = document.title,
            workspaceName = document.workspace.title,
        )
    }

    @Transactional
    fun updateTitle(
        documentId: Long,
        request: DocumentTitleRequest,
    ): DocumentMetadataResponse {
        val document = documentRepository.findById(documentId).orElseThrow { DocumentNotFoundException() }
        document.title = request.title
        document.updatedAt = LocalDateTime.now()
        return DocumentMetadataResponse(
            documentId = document.id!!,
            title = document.title,
            workspaceName = document.workspace.title,
        )
    }
}
