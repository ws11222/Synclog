package com.example.synclog.document.controller

import com.example.synclog.common.exception.DocumentNotFoundException
import com.example.synclog.document.persistence.DocumentContentRepository
import com.example.synclog.document.service.DocumentManager
import org.springframework.stereotype.Component
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.BinaryWebSocketHandler
import kotlin.jvm.optionals.getOrNull

@Component
class DocumentSocketHandler(
    private val docManager: DocumentManager,
    private val documentContentRepository: DocumentContentRepository,
) : BinaryWebSocketHandler() {
    // 1. 연결 시: 캐시나 DB에서 기존 Yjs 바이너리를 꺼내 클라이언트에게 전송
    override fun afterConnectionEstablished(session: WebSocketSession) {
        val docId = getDocId(session)
        docManager.addSession(docId, session)

        // 1. DB에서 full binary 가져오기
        if (!documentContentRepository.existsById(docId)) throw DocumentNotFoundException()
        val initialState = documentContentRepository.findById(docId).getOrNull()?.yjsBinary

        // 2. 데이터가 있다면 클라이언트에게 전송
        if (initialState != null) {
            session.sendMessage(BinaryMessage(initialState))
        }
    }

    // 2. 메시지 수신 시: 다른 유저에게 전달(Relay) 및 버퍼 저장
    override fun handleBinaryMessage(
        session: WebSocketSession,
        message: BinaryMessage,
    ) {
        val docId = getDocId(session)
        val payload = message.payload
        val bytes = payload.array()

        if (bytes.isEmpty()) return

        val messageType = bytes[0].toInt()

        // [Relay] 나를 제외한 같은 방 사람들에게 전송
        docManager.getSessions(docId).forEach { s ->
            if (s.isOpen && s.id != session.id) {
                s.sendMessage(BinaryMessage(payload))
            }
        }

        // [Buffer] 나중에 DB에 저장할 수 있도록 메모리에 임시 보관
        if (messageType == 0) {
            docManager.appendUpdate(docId, bytes)
        }
    }

    // 3. 연결이 끊겼을 때
    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus,
    ) {
        val docId = getDocId(session)
        docManager.removeSession(docId, session)
    }

    private fun getDocId(session: WebSocketSession): Long {
        return session.uri?.path?.substringAfterLast("/")?.toLong() ?: throw DocumentNotFoundException()
    }
}
