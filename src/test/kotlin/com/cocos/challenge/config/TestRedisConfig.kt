package com.cocos.challenge.config

import org.mockito.kotlin.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.core.StringRedisTemplate

@TestConfiguration
class TestRedisConfig {
    @Bean
    fun stringRedisTemplate(): StringRedisTemplate = mock()
}
