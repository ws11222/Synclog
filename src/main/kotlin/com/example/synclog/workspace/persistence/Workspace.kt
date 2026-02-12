package com.example.synclog.workspace.persistence

import com.example.synclog.document.persistence.Document
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany

@Entity
class Workspace(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var title: String,
    @OneToMany(mappedBy = "workspace", cascade = [CascadeType.ALL], orphanRemoval = true)
    val members: MutableList<WorkspaceMember> = mutableListOf(),
    @OneToMany(mappedBy = "workspace", cascade = [CascadeType.ALL])
    val documents: MutableList<Document> = mutableListOf(),
)
