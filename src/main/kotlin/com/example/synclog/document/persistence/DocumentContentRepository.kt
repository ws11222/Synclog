package com.example.synclog.document.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface DocumentContentRepository : JpaRepository<DocumentContent, Long> {
    @Modifying
    @Query(
        """
        UPDATE DocumentContent dc 
        SET dc.yjsBinary = CASE 
            WHEN dc.yjsBinary IS NULL THEN :newUpdate
            ELSE dc.yjsBinary || :newUpdate
        END 
        WHERE dc.document.id = :docId
    """,
        nativeQuery = true,
    )
    fun appendYjsUpdate(
        docId: Long,
        newUpdate: ByteArray,
    )

    @Query(
        value = """
            SELECT dc.* FROM document_content dc
            JOIN document d ON dc.document_id = d.id
            WHERE d.workspace_id = :workspaceId
            ORDER BY dc.embedding <=> CAST(:queryVector AS vector)
            LIMIT :limit
        """,
        nativeQuery = true,
    )
    fun findSimilarContents(
        workspaceId: Long,
        queryVector: FloatArray,
        limit: Int,
    ): List<DocumentContent>
}
