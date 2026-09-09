package com.cocos.challenge.application.exception

/**
 * Base type for missing-resource errors surfaced by the use cases (unknown user,
 * instrument, order, missing market data, etc.).
 *
 * Concrete subclasses carry the specific resource identifier so the throw site
 * stays expressive; the exception handler only needs to know about this base
 * type to map every subtype to HTTP `404`.
 */
open class NotFoundException(message: String) : RuntimeException(message)
