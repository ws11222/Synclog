package com.example.synclog.common.ai

import com.example.synclog.common.exception.DomainException
import org.springframework.ai.document.Document
import org.springframework.ai.embedding.Embedding
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.embedding.EmbeddingRequest
import org.springframework.ai.embedding.EmbeddingResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.util.concurrent.Semaphore

@Component
class CustomEmbeddingModel(
    private val huggingFaceWebClinet: WebClient,
) : EmbeddingModel {
    private val apiKey = System.getenv("HF_API_KEY")
    private val maxConcurrency = (System.getenv("HF_MAX_CONCURRENCY")?.toIntOrNull() ?: 3).coerceAtLeast(1)
    private val limiter = Semaphore(maxConcurrency, true)

    override fun embed(text: String): FloatArray {
        if (!limiter.tryAcquire()) {
            throw DomainException(HttpStatus.TOO_MANY_REQUESTS, "Embedding service is busy. Please retry shortly.")
        }

        val requestBody =
            mapOf(
                "inputs" to listOf(text),
                "options" to mapOf("wait_for_model" to true),
            )
        return try {
            val response =
                huggingFaceWebClinet.post()
                    .uri("/hf-inference/models/BAAI/bge-m3/pipeline/feature-extraction")
                    .header("Authorization", "Bearer $apiKey")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(List::class.java)
                    .block() // 단순 구현을 위해 동기 처리 (실제 서비스에선 비동기 고려)

            val embedding =
                if (response?.get(0) is List<*>) {
                    response[0] as List<Double>
                } else {
                    response as List<Double>
                }

            embedding.map { it.toFloat() }.toFloatArray()
        } catch (_: Exception) {
            throw DomainException(HttpStatus.SERVICE_UNAVAILABLE, "Embedding service unavailable.")
        } finally {
            limiter.release()
        }
    }

    override fun embed(document: Document): FloatArray {
        val content = document.text
        if (content.isNullOrBlank()) {
            return floatArrayOf()
        }
        return embed(content)
    }

    override fun call(request: EmbeddingRequest): EmbeddingResponse {
        val embeddings =
            request.instructions.mapIndexed { index, text ->
                Embedding(embed(text), index)
            }
        return EmbeddingResponse(embeddings)
    }
}
