package com.example.synclog.document.service

import com.example.synclog.document.persistence.DocumentContentRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DocumentScheduler(
    private val documentManager: DocumentManager,
    private val documentContentRepository: DocumentContentRepository,
) {
    @Scheduled(fixedRate = 5000)
    @Transactional
    fun flushUpdates() {
        val activeDocIds = documentManager.getActiveDocIds()
        activeDocIds.forEach { docId ->
            // 1. 버퍼에서 업데이트 로그 뭉치 가져오기 (원자적 Swap)
            val updates = documentManager.drainUpdates(docId)
            if (updates.isEmpty()) return@forEach

            // 2. 여러 개의 업데이트 ByteArray를 하나로 합침
            // Yjs 업데이트는 단순히 바이트를 이어 붙여도 유효한 업데이트 로그가 됨
            val mergedUpdate = combineByteArrays(updates)

            // 3. DB 업데이트: 기존 바이너리 끝에 새 바이너리를 이어 붙임 (PostgreSQL 기능 활용 가능)
            documentContentRepository.appendYjsUpdate(docId, mergedUpdate)
        }
    }

    private fun combineByteArrays(updates: List<ByteArray>): ByteArray {
        val totalLength = updates.sumOf { it.size }
        val result = ByteArray(totalLength)
        var currentPos = 0
        for (update in updates) {
            System.arraycopy(update, 0, result, currentPos, update.size)
            currentPos += update.size
        }
        return result
    }
}
