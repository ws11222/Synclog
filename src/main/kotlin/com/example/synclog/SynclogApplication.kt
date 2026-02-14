package com.example.synclog

import org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication(
    exclude = [
        PgVectorStoreAutoConfiguration::class,
    ],
)
@EnableAsync
class SynclogApplication

fun main(args: Array<String>) {
    runApplication<SynclogApplication>(*args)
}
