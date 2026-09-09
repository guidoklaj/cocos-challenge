package com.cocos.challenge.application.exception

class OrderNotFoundException(orderId: Int) :
    NotFoundException("Order $orderId not found")
