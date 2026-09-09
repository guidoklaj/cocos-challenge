package com.cocos.challenge.domain.exception

class OrderNotCancellableException(orderId: Int, status: String) :
    ValidationException("Order $orderId cannot be cancelled because its status is $status")
