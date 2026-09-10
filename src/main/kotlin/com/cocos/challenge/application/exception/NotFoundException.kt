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

class InstrumentNotFoundException(identifier: String) :
    NotFoundException("Instrument '$identifier' not found")

class MarketDataNotFoundException(instrumentId: Int) :
    NotFoundException("No market data available for instrument $instrumentId")

class OrderNotFoundException(orderId: Int) :
    NotFoundException("Order $orderId not found")

class UserNotFoundException(userId: Int) :
    NotFoundException("User $userId not found")
