package com.cocos.challenge.domain.exception

/**
 * Base type for domain-rule violations — e.g. attempting to BUY a MONEDA
 * instrument, cancelling a non-`NEW` order, or ordering a fractional share.
 *
 * Concrete subclasses live alongside so each throw site stays expressive; the
 * exception handler only needs to know about this base type to map every
 * subtype to HTTP `422`.
 */
open class ValidationException(message: String) : RuntimeException(message)
