package com.plcoding.ktspringboot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KtSpringBootApplication

fun main(args: Array<String>) {
	runApplication<KtSpringBootApplication>(*args)
}
