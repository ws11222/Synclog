package com.example.synclog.workspace.controller

import com.example.synclog.workspace.service.WorkspaceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class WorkspaceController(
    private val workspaceService: WorkspaceService,
) {
    @PostMapping("/workspace/create")
    fun createWorkspace(
        @RequestAttribute("userId") userId: String,
        @RequestBody requestDto: CreateWorkspaceRequest,
    ): ResponseEntity<WorkspaceResponse> {
        val response = workspaceService.createWorkspace(userId, requestDto)
        return ResponseEntity.ok(response)
    }
}
