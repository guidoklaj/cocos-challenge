package com.cocos.challenge.infrastructure.aspect

import com.cocos.challenge.api.annotation.Idempotent
import com.cocos.challenge.api.exception.MissingIdempotencyKeyException
import com.fasterxml.jackson.databind.ObjectMapper
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.lang.reflect.ParameterizedType
import java.time.Duration

@Aspect
@Component
class IdempotencyAspect(
    private val redis: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Around("@annotation(idempotent)")
    fun handle(pjp: ProceedingJoinPoint, idempotent: Idempotent): Any? {
        val key = idempotencyKey() ?: throw MissingIdempotencyKeyException()

        val redisKey = "idempotency:$key"

        val cached = try { redis.opsForValue().get(redisKey) } catch (_: Exception) {
            log.warn("Redis unavailable for idempotency check on key={}, proceeding without cache", key)
            return pjp.proceed()
        }

        if (cached != null) return deserialize(cached, pjp)

        val result = pjp.proceed()

        try {
            redis.opsForValue().set(redisKey, serialize(result), Duration.ofSeconds(idempotent.ttlSeconds))
        } catch (_: Exception) {
            log.warn("Redis unavailable, idempotency key={} not stored", key)
        }

        return result
    }

    private fun serialize(result: Any?): String {
        if (result is ResponseEntity<*>) {
            return objectMapper.writeValueAsString(
                mapOf("status" to result.statusCode.value(), "body" to result.body)
            )
        }
        return objectMapper.writeValueAsString(result)
    }

    private fun deserialize(cached: String, pjp: ProceedingJoinPoint): Any? {
        val method = (pjp.signature as MethodSignature).method
        if (ResponseEntity::class.java.isAssignableFrom(method.returnType)) {
            val wrapper = objectMapper.readTree(cached)
            val status = wrapper.get("status").asInt()
            val bodyType = (method.genericReturnType as? ParameterizedType)?.actualTypeArguments?.get(0)
            val body = bodyType
                ?.let { objectMapper.treeToValue(wrapper.get("body"), objectMapper.typeFactory.constructType(it)) }
                ?: objectMapper.treeToValue(wrapper.get("body"), Any::class.java)
            return ResponseEntity.status(status).body(body)
        }
        return objectMapper.readValue(cached, method.returnType)
    }

    private fun idempotencyKey(): String? =
        (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)
            ?.request
            ?.getHeader("Idempotency-Key")
}
