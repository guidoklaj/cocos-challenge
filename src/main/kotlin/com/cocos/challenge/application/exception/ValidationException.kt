package com.cocos.challenge.application.exception

/**
 * Base type for domain-rule violations — e.g. attempting to BUY a CURRENCY
 * instrument, cancelling a non-`NEW` order, or ordering a fractional share.
 *
 * Concrete subclasses live alongside so each throw site stays expressive; the
 * exception handler only needs to know about this base type to map every
 * subtype to HTTP `422`.
 */
open class ValidationException(message: String) : RuntimeException(message)

class OrderNotCancellableException(orderId: Int, status: String) :
    ValidationException("Order $orderId cannot be cancelled because its status is $status")

class InvalidOrderException(message: String) : ValidationException(message)
