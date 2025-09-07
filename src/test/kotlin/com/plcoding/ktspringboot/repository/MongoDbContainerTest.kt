package com.plcoding.ktspringboot.repository

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
abstract class MongoDbContainerTest {
    companion object {
        @Container
        @JvmStatic
        private val mongo: MongoDBContainer = MongoDBContainer("mongo:7.0.14").withReuse(true)

        init {
            // Ensures the container starts once per test JVM
            mongo.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun mongoProps(registry: DynamicPropertyRegistry) {
            // Use replicaSetUrl for full-featured Mongo behavior
            registry.add("spring.data.mongodb.uri") { mongo.replicaSetUrl }
        }
    }

}