package com.cocos.challenge.api.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Idempotent(val ttlSeconds: Long = 86400)
