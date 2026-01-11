package com.example.synclog.workspace.controller

import com.example.synclog.document.controller.DocumentSimpleResponse
import com.example.synclog.workspace.persistence.Workspace
import java.time.LocalDateTime

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

enum class WorkspaceRole(val priority: Int) {
    OWNER(3), // 모든 권한, 삭제 가능
    ADMIN(2), // 멤버 초대/제거, 문서 관리
    MEMBER(1), // 문서 읽기/쓰기
    ;

    fun canManageMembers(): Boolean = this.priority >= ADMIN.priority

    fun canDeleteWorkspace(): Boolean = this == OWNER
}

data class MemberResponse(
    val userId: String,
    val name: String,
    val email: String,
    val role: WorkspaceRole,
    val joinedAt: LocalDateTime,
)

data class RoleUpdateRequest(
    val userId: String,
    val role: WorkspaceRole,
)

data class InviteRequest(
    val email: String,
    val role: WorkspaceRole,
)
