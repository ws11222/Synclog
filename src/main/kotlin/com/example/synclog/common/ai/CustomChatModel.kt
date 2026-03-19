package com.example.synclog.common.ai

import com.example.synclog.common.exception.DomainException
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.net.SocketTimeoutException
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeoutException

@Component
class CustomChatModel(
    private val huggingFaceWebClient: WebClient,
) : ChatModel {
    private val apiKey = System.getenv("HF_API_KEY")
    private val maxConcurrency = (System.getenv("HF_MAX_CONCURRENCY")?.toIntOrNull() ?: 3).coerceAtLeast(1)
    private val limiter = Semaphore(maxConcurrency, true)
    private val maxRetries = (System.getenv("HF_MAX_RETRIES")?.toIntOrNull() ?: 1).coerceAtLeast(0)
    private val retryBackoffMillis = (System.getenv("HF_RETRY_BACKOFF_MS")?.toLongOrNull() ?: 250L).coerceAtLeast(0L)

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
                executeWithRetry {
                    huggingFaceWebClient.post()
                        .uri("/v1/chat/completions")
                        .header("Authorization", "Bearer $apiKey")
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(Map::class.java)
                        .block() ?: throw RuntimeException("AI 응답을 받지 못했습니다.")
                }

            val choices = response["choices"] as List<Map<String, Any>>
            val message = choices[0]["message"] as Map<String, Any>
            val content = message["content"] as String

            val generation = Generation(AssistantMessage(content))
            ChatResponse(listOf(generation))
        } catch (ex: WebClientResponseException) {
            if (ex.statusCode == HttpStatus.PAYMENT_REQUIRED) {
                throw DomainException(HttpStatus.BAD_GATEWAY, "Chat provider quota exceeded.")
            }
            throw DomainException(HttpStatus.SERVICE_UNAVAILABLE, "Chat service unavailable.")
        } catch (_: Exception) {
            throw DomainException(HttpStatus.SERVICE_UNAVAILABLE, "Chat service unavailable.")
        } finally {
            limiter.release()
        }
    }

    private fun <T> executeWithRetry(block: () -> T): T {
        var attempt = 0
        var lastException: Exception? = null

        while (attempt <= maxRetries) {
            try {
                return block()
            } catch (ex: Exception) {
                if (!isRetryable(ex) || attempt == maxRetries) {
                    throw ex
                }
                lastException = ex
                Thread.sleep(retryBackoffMillis * (attempt + 1))
                attempt++
            }
        }

        throw lastException ?: IllegalStateException("Retry block failed without exception")
    }

    private fun isRetryable(ex: Exception): Boolean =
        ex is WebClientResponseException.BadGateway ||
            ex is WebClientResponseException.ServiceUnavailable ||
            ex is WebClientResponseException.GatewayTimeout ||
            ex is SocketTimeoutException ||
            ex is TimeoutException
}
