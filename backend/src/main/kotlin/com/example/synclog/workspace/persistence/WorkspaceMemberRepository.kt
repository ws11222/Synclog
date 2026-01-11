package com.example.synclog.workspace.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface WorkspaceMemberRepository : JpaRepository<WorkspaceMember, Long> {
    @Query("select m from WorkspaceMember m join fetch m.workspace where m.user.id = :userId")
    fun findAllByUserIdWithWorkspace(userId: String): List<WorkspaceMember>

    @Query("select  m from WorkspaceMember m join fetch m.user where m.workspace.id = :workspaceId")
    fun findAllByWorkspaceIdWithUser(workspaceId: Long): List<WorkspaceMember>

    @Query("select  m from WorkspaceMember m join fetch m.user where m.user.id = :userId and m.workspace.id = :workspaceId")
    fun findByWorkspaceIdWithUser(
        userId: String,
        workspaceId: Long,
    ): WorkspaceMember?

    fun findByUserIdAndWorkspaceId(
        userId: String,
        workspaceId: Long,
    ): WorkspaceMember?

    fun existsByUserIdAndWorkspaceId(
        userId: String,
        workspaceId: Long,
    ): Boolean
}
