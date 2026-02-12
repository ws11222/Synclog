package com.example.synclog.document.service

import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

@Service
class DocumentManager {
    // 문서 ID별 접속 세션 관리 (Thread-safe)
    private val sessionsMap = ConcurrentHashMap<Long, MutableSet<WebSocketSession>>()

    // DB 부하 방지를 위한 임시 저장소 (Buffer)
    private val binaryBuffer = ConcurrentHashMap<Long, MutableList<ByteArray>>()

    fun addSession(
        docId: Long,
        session: WebSocketSession,
    ) {
        sessionsMap.getOrPut(docId) { CopyOnWriteArraySet() }.add(session)
    }

    fun removeSession(
        docId: Long,
        session: WebSocketSession,
    ) {
        sessionsMap[docId]?.remove(session)
    }

    fun getSessions(docId: Long): Set<WebSocketSession> = sessionsMap[docId] ?: emptySet()

    fun appendUpdate(
        docId: Long,
        binary: ByteArray,
    ) {
        binaryBuffer.getOrPut(docId) { CopyOnWriteArrayList() }.add(binary)
    }

    fun drainUpdates(docId: Long): List<ByteArray> {
        val updates = binaryBuffer.remove(docId)
        return updates ?: emptyList()
    }

    fun getActiveDocIds() = binaryBuffer.keys().toList()
}
