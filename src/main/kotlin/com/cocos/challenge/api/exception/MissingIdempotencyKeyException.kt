package com.cocos.challenge.api.exception

class MissingIdempotencyKeyException : RuntimeException("Idempotency-Key header is required")
