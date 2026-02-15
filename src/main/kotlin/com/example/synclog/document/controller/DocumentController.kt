package com.example.synclog.document.controller

import com.example.synclog.document.service.DocumentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/document")
class DocumentController(
    private val documentService: DocumentService,
) {
    @PostMapping("/create/{workspaceId}")
    fun createDocument(
        @RequestAttribute("userId") userId: String,
        @PathVariable workspaceId: Long,
    ): ResponseEntity<DocumentSimpleResponse> {
        val response = documentService.createDocument(workspaceId)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{documentId}/snapshot")
    fun saveFullSnapshot(
        @RequestAttribute("userId") userId: String,
        @PathVariable documentId: Long,
        @RequestBody request: DocumentSnapshotRequest,
    ): ResponseEntity<Unit> {
        val decodedBinary = java.util.Base64.getMimeDecoder().decode(request.fullBinary)
        documentService.saveFullSnapshot(documentId, request.plainText, decodedBinary)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{documentId}/metadata")
    fun getDocumentMetadata(
        @RequestAttribute("userId") userId: String,
        @PathVariable documentId: Long,
    ): ResponseEntity<DocumentMetadataResponse> {
        val metadata = documentService.getMetadata(documentId)
        return ResponseEntity.ok(metadata)
    }

    @PatchMapping("/{documentId}/update/title")
    fun updateDocumentTitle(
        @RequestAttribute("userId") userId: String,
        @PathVariable documentId: Long,
        @RequestBody request: DocumentTitleRequest,
    ): ResponseEntity<DocumentMetadataResponse> {
        val metadata = documentService.updateTitle(documentId, request)
        return ResponseEntity.ok(metadata)
    }

    @GetMapping("/{documentId}/rag")
    fun ragDocument(
        @RequestAttribute("userId") userId: String,
        @PathVariable documentId: Long,
        @RequestBody request: DocumentRagRequest,
    ): ResponseEntity<DocumentRagResponse> {
        val response = documentService.rag(documentId, request.request)
        return ResponseEntity.ok(response)
    }
}
