package com.example.synclog.workspace.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface WorkspaceMemberRepository : JpaRepository<WorkspaceMember, Long> {
    @Query("select m from WorkspaceMember m join fetch m.workspace where m.user.id = :userId")
    fun findAllByUserIdWithWorkspace(userId: String): List<WorkspaceMember>
}
