package com.example.synclog.workspace.service

import com.example.synclog.common.exception.UserNotFoundException
import com.example.synclog.user.persistence.UserRepository
import com.example.synclog.workspace.controller.CreateWorkspaceRequest
import com.example.synclog.workspace.controller.WorkspaceResponse
import com.example.synclog.workspace.persistence.Workspace
import com.example.synclog.workspace.persistence.WorkspaceMember
import com.example.synclog.workspace.persistence.WorkspaceMemberRepository
import com.example.synclog.workspace.persistence.WorkspaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class WorkspaceService(
    private val workspaceRepository: WorkspaceRepository,
    private val userRepository: UserRepository,
    private val workspaceMemberRepository: WorkspaceMemberRepository,
) {
    @Transactional
    fun createWorkspace(
        userId: String,
        requestDTO: CreateWorkspaceRequest,
    ): WorkspaceResponse {
        println("DEBUG: Looking for User with ID = [$userId]")
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }

        val workspace = workspaceRepository.save(Workspace(title = requestDTO.title))
        val workspaceMember =
            WorkspaceMember(
                user = user,
                workspace = workspace,
                createdAt = LocalDateTime.now(),
            )
        workspaceMemberRepository.save(workspaceMember)
        return WorkspaceResponse.fromEntity(workspace)
    }
}
