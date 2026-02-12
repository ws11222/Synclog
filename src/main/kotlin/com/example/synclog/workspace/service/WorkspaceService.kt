package com.example.synclog.workspace.service

import com.example.synclog.common.exception.MemberNotFoundException
import com.example.synclog.common.exception.NotEnoughRoleException
import com.example.synclog.common.exception.UserNotFoundException
import com.example.synclog.common.exception.WorkspaceNotFoundException
import com.example.synclog.document.controller.DocumentSimpleResponse
import com.example.synclog.user.persistence.UserRepository
import com.example.synclog.workspace.controller.CreateWorkspaceRequest
import com.example.synclog.workspace.controller.InviteRequest
import com.example.synclog.workspace.controller.MemberResponse
import com.example.synclog.workspace.controller.RoleUpdateRequest
import com.example.synclog.workspace.controller.WorkspaceResponse
import com.example.synclog.workspace.controller.WorkspaceRole
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
        request: CreateWorkspaceRequest,
    ): WorkspaceResponse {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }

        val workspace = workspaceRepository.save(Workspace(title = request.title))
        val workspaceMember =
            WorkspaceMember(
                user = user,
                workspace = workspace,
                role = WorkspaceRole.OWNER,
                joinedAt = LocalDateTime.now(),
            )
        workspaceMemberRepository.save(workspaceMember)
        return WorkspaceResponse.fromEntity(workspace)
    }

    @Transactional
    fun getWorkspaces(userId: String): List<WorkspaceResponse> {
        val members = workspaceMemberRepository.findAllByUserIdWithWorkspace(userId)
        return members.map { member ->
            val workspace = member.workspace
            WorkspaceResponse(
                id = workspace.id!!,
                title = workspace.title,
                memberCount = workspace.members.size,
                documentCount = workspace.documents.size,
                documents = workspace.documents.map { document -> DocumentSimpleResponse.fromEntity(document) },
            )
        }
    }

    @Transactional
    fun getMembers(workspaceId: Long): List<MemberResponse> {
        val members = workspaceMemberRepository.findAllByWorkspaceIdWithUser(workspaceId)
        return members.map {
            MemberResponse(
                it.user.id!!,
                it.user.name,
                it.user.email,
                it.role,
                it.joinedAt,
            )
        }
    }

    @Transactional
    fun updateRole(
        userId: String,
        workspaceId: Long,
        request: RoleUpdateRequest,
    ) {
        val requester = workspaceMemberRepository.findByUserIdAndWorkspaceId(userId, workspaceId) ?: throw MemberNotFoundException()
        if (requester.role != WorkspaceRole.OWNER) throw NotEnoughRoleException()
        val target = workspaceMemberRepository.findByWorkspaceIdWithUser(request.userId, workspaceId) ?: throw MemberNotFoundException()
        target.role = request.role
    }

    @Transactional
    fun inviteMember(
        userId: String,
        workspaceId: Long,
        request: InviteRequest,
    ) {
        val requester = workspaceMemberRepository.findByUserIdAndWorkspaceId(userId, workspaceId) ?: throw MemberNotFoundException()
        if (!requester.role.canManageMembers()) throw NotEnoughRoleException()
        val targetUser = userRepository.findByEmail(request.email) ?: throw UserNotFoundException()
        val workspace = workspaceRepository.findById(workspaceId).orElseThrow { WorkspaceNotFoundException() }
        if (!workspaceMemberRepository.existsByUserIdAndWorkspaceId(targetUser.id!!, workspaceId)) {
            workspaceMemberRepository.save(
                WorkspaceMember(
                    user = targetUser,
                    workspace = workspace,
                    role = request.role,
                    joinedAt = LocalDateTime.now(),
                ),
            )
        }
    }
}
