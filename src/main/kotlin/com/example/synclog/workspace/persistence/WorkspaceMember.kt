package com.example.synclog.workspace.persistence

import com.example.synclog.user.persistence.User
import com.example.synclog.workspace.controller.WorkspaceRole
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "workspace_member")
class WorkspaceMember(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    val workspace: Workspace,
    @Enumerated(EnumType.STRING)
    var role: WorkspaceRole = WorkspaceRole.MEMBER,
    val joinedAt: LocalDateTime = LocalDateTime.now(),
) {
    init {
        workspace.members.add(this)
    }
}
