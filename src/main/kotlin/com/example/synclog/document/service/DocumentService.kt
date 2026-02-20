package com.example.synclog.document.service

import com.example.synclog.common.exception.DocumentNotFoundException
import com.example.synclog.common.exception.MemberNotFoundException
import com.example.synclog.common.exception.NotEnoughRoleException
import com.example.synclog.common.exception.WorkspaceNotFoundException
import com.example.synclog.document.controller.DocumentMetadataResponse
import com.example.synclog.document.controller.DocumentRagResponse
import com.example.synclog.document.controller.DocumentSimpleResponse
import com.example.synclog.document.controller.DocumentTaskResponse
import com.example.synclog.document.controller.DocumentTitleRequest
import com.example.synclog.document.persistence.Document
import com.example.synclog.document.persistence.DocumentContent
import com.example.synclog.document.persistence.DocumentContentRepository
import com.example.synclog.document.persistence.DocumentRepository
import com.example.synclog.workspace.controller.WorkspaceRole
import com.example.synclog.workspace.persistence.WorkspaceMemberRepository
import com.example.synclog.workspace.persistence.WorkspaceRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.socket.CloseStatus
import java.time.LocalDateTime

@Service
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val documentContentRepository: DocumentContentRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val workspaceMemberRepository: WorkspaceMemberRepository,
    private val embeddingModel: EmbeddingModel,
    private val chatModel: ChatModel,
    private val documentManager: DocumentManager,
) {
    @Transactional
    fun createDocument(workspaceId: Long): DocumentSimpleResponse {
        val workspace = workspaceRepository.findById(workspaceId).orElseThrow { WorkspaceNotFoundException() }
        val document =
            Document(
                title = "새 문서",
                workspace = workspace,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        val newContent = DocumentContent(document = document, plainText = "", yjsBinary = ByteArray(0), embedding = null)
        document.content = newContent
        documentRepository.save(document)
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

    @Transactional
    fun rag(
        documentId: Long,
        request: String,
    ): DocumentRagResponse {
        val document = documentRepository.findById(documentId).orElseThrow { DocumentNotFoundException() }
        val similarContents =
            documentContentRepository.findSimilarContents(
                document.workspace.id!!,
                embeddingModel.embed(request),
                3,
            )

        val context = similarContents.joinToString(separator = "\n\n") { it.plainText!! }

        val prompt: Prompt =
            Prompt(
                """
                너는 회의록 분석 전문가야. 아래 제공된 [회의록 내용]을 바탕으로 사용자의 [질문]에 대해 답변해줘.
                만약 답변을 위한 정보가 회의록 내용에 없다면, "관련 내용을 찾을 수 없습니다"라고 답해줘.
                
                [회의록 내용]
                $context
                
                [질문]
                $request
                
                답변:
                """.trimIndent(),
            )

        val response = chatModel.call(prompt)
        return DocumentRagResponse(response.result.output.content)
    }

    @Transactional
    fun deleteDocument(
        documentId: Long,
        userId: String,
    ) {
        val document = documentRepository.findById(documentId).orElseThrow { DocumentNotFoundException() }
        val member =
            workspaceMemberRepository.findByUserIdAndWorkspaceId(
                userId,
                document.workspace.id!!,
            ) ?: throw MemberNotFoundException()

        if (member.role > WorkspaceRole.ADMIN) {
            throw NotEnoughRoleException()
        }

        documentManager.getSessions(documentId).forEach { session ->
            if (session.isOpen) {
                session.close(CloseStatus.NORMAL.withReason("Documet deleted"))
            }
        }
        documentManager.clearResource(documentId)
        documentRepository.delete(document)
    }

    @Transactional
    fun getTasks(
        userId: String,
        documentId: Long,
    ): DocumentTaskResponse {
        val content = documentContentRepository.findById(documentId).orElseThrow { DocumentNotFoundException() }
        val prompt: Prompt =
            Prompt(
                """
                너는 회의록 분석 전문가야. [회의록 내용]을 보고 [출력 형식]에 맞는 데이터를 추출해줘. 
                추출할 작업이 없으면 {"response": []}를 반환해줘. 
                Return ONLY JSON. No talk. No explanation.
                
                [출력 형식]:
                반드시 아래 JSON 스키마를 따르는 유효한 JSON 데이터만 응답해줘. 다른 설명은 생략해.

                JSON
                {
                  "response": [
                    {
                      "title": "할 일 내용",
                      "name": "담당자 이름(없으면 null)",
                      "date": "2026-02-19 (ISO_LOCAL_DATE 형식, 없으면 null)"
                    }
                  ]
                }
                
                [회의록 내용]
                ${content.plainText}
                
                답변:
                """.trimIndent(),
            )

        val response = chatModel.call(prompt).result.output.content
        val objectMapper =
            ObjectMapper()
                .registerKotlinModule()
                .registerModule(JavaTimeModule())

        return try {
            val cleanedJson =
                response
                    .substringAfter("```json")
                    .substringAfter("```")
                    .substringBeforeLast("```")
                    .trim()

            objectMapper.readValue(cleanedJson, DocumentTaskResponse::class.java)
        } catch (e: Exception) {
            println("Parsing Error: ${e.message} | Raw Content: $response")
            DocumentTaskResponse(response = emptyList())
        }
    }
}
