package com.example.synclog.document.controller

import com.example.synclog.document.service.DocumentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class DocumentController(
    private val documentService: DocumentService,
) {
    @PostMapping("/document/create/{workspaceId}")
    fun createDocument(
        @RequestAttribute("userId") userId: String,
        @PathVariable workspaceId: Long,
    ): ResponseEntity<DocumentSimpleResponse> {
        val response = documentService.createDocument(workspaceId)
        return ResponseEntity.ok(response)
    }
}
