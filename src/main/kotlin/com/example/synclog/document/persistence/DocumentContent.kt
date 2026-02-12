package com.example.synclog.document.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "document_content")
class DocumentContent(
    @Id
    val id: Long? = null,
    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    val document: Document,
    @Column(columnDefinition = "TEXT")
    var plainText: String? = null,
    @Column(columnDefinition = "BYTEA")
    var yjsBinary: ByteArray? = null,
)
