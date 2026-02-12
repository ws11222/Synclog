package com.example.synclog.common.config

import com.example.synclog.document.controller.DocumentSocketHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val documentSocketHandler: DocumentSocketHandler,
) : WebSocketConfigurer {
    // 클라이언트는 ws://localhost:8080/ws/docs/{documentId} 로 접속
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(documentSocketHandler, "/ws/docs/{documentId}").setAllowedOrigins("*")
    }
}
