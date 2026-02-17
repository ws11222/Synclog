package com.example.synclog.workspace.controller

import com.example.synclog.workspace.service.WorkspaceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workspace")
class WorkspaceController(
    private val workspaceService: WorkspaceService,
) {
    @PostMapping("/create")
    fun createWorkspace(
        @RequestAttribute("userId") userId: String,
        @RequestBody request: CreateWorkspaceRequest,
    ): ResponseEntity<WorkspaceResponse> {
        val response = workspaceService.createWorkspace(userId, request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/get")
    fun getWorkspaces(
        @RequestAttribute("userId") userId: String,
    ): ResponseEntity<List<WorkspaceResponse>> {
        val response = workspaceService.getWorkspaces(userId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{workspaceId}/members")
    fun getMembers(
        @PathVariable workspaceId: Long,
    ): ResponseEntity<List<MemberResponse>> {
        return ResponseEntity.ok(workspaceService.getMembers(workspaceId))
    }

    @PutMapping("/{workspaceId}/update/role")
    fun updateRole(
        @RequestAttribute("userId") userId: String,
        @PathVariable workspaceId: Long,
        @RequestBody request: RoleUpdateRequest,
    ): ResponseEntity<Unit> {
        workspaceService.updateRole(userId, workspaceId, request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{workspaceId}/invite")
    fun inviteMember(
        @RequestAttribute("userId") userId: String,
        @PathVariable workspaceId: Long,
        @RequestBody request: InviteRequest,
    ): ResponseEntity<Unit> {
        workspaceService.inviteMember(userId, workspaceId, request)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{workspaceId}/delete")
    fun deleteWorkspace(
        @RequestAttribute("userId") userId: String,
        @PathVariable workspaceId: Long,
    ): ResponseEntity<Unit> {
        workspaceService.deleteWorkspace(userId, workspaceId)
        return ResponseEntity.ok().build()
    }
}
