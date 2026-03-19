package com.example.synclog.common.ai

import com.example.synclog.common.exception.DomainException
import org.springframework.http.HttpStatus
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.util.concurrent.Semaphore

@Component
class CustomChatModel(
    private val huggingFaceWebClient: WebClient,
) : ChatModel {
    private val apiKey = System.getenv("HF_API_KEY")
    private val maxConcurrency = (System.getenv("HF_MAX_CONCURRENCY")?.toIntOrNull() ?: 3).coerceAtLeast(1)
    private val limiter = Semaphore(maxConcurrency, true)

    override fun call(prompt: Prompt): ChatResponse {
        if (!limiter.tryAcquire()) {
            throw DomainException(HttpStatus.TOO_MANY_REQUESTS, "Chat service is busy. Please retry shortly.")
        }

        val messages =
            prompt.instructions.map {
                mapOf("role" to it.messageType.name.lowercase(), "content" to it.content)
            }

        val requestBody =
            mapOf(
                "model" to "deepseek-ai/DeepSeek-V3.2:novita",
                "messages" to messages,
                "stream" to false,
            )

        return try {
            val response =
                huggingFaceWebClient.post()
                    .uri("/v1/chat/completions")
                    .header("Authorization", "Bearer $apiKey")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map::class.java)
                    .block() ?: throw RuntimeException("AI 응답을 받지 못했습니다.")

            val choices = response["choices"] as List<Map<String, Any>>
            val message = choices[0]["message"] as Map<String, Any>
            val content = message["content"] as String

            val generation = Generation(AssistantMessage(content))
            ChatResponse(listOf(generation))
        } catch (_: Exception) {
            throw DomainException(HttpStatus.SERVICE_UNAVAILABLE, "Chat service unavailable.")
        } finally {
            limiter.release()
        }
    }
}
