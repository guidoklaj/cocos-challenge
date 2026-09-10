package com.cocos.challenge.infrastructure.aspect

import com.cocos.challenge.api.annotation.Idempotent
import com.fasterxml.jackson.databind.ObjectMapper
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.Duration

@Aspect
@Component
@ConditionalOnBean(StringRedisTemplate::class)
class IdempotencyAspect(
    private val redis: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Around("@annotation(idempotent)")
    fun handle(pjp: ProceedingJoinPoint, idempotent: Idempotent): Any? {
        val key = idempotencyKey() ?: return pjp.proceed()
        val redisKey = "idempotency:$key"

        val cached = try { redis.opsForValue().get(redisKey) } catch (e: Exception) {
            log.warn("Redis unavailable for idempotency check on key={}, proceeding without cache", key)
            return pjp.proceed()
        }

        if (cached != null) {
            val returnType = (pjp.signature as MethodSignature).returnType
            return objectMapper.readValue(cached, returnType)
        }

        val result = pjp.proceed()

        try {
            redis.opsForValue().set(
                redisKey,
                objectMapper.writeValueAsString(result),
                Duration.ofSeconds(idempotent.ttlSeconds)
            )
        } catch (e: Exception) {
            log.warn("Redis unavailable, idempotency key={} not stored", key)
        }

        return result
    }

    private fun idempotencyKey(): String? =
        (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)
            ?.request
            ?.getHeader("Idempotency-Key")
}
