package com.example.synclog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SynclogApplication

fun main(args: Array<String>) {
	runApplication<SynclogApplication>(*args)
}
