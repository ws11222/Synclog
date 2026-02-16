package com.example.synclog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class SynclogApplication

fun main(args: Array<String>) {
    runApplication<SynclogApplication>(*args)
}
