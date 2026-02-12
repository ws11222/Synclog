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
}
