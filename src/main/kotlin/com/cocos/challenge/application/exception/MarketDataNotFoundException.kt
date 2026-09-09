package com.cocos.challenge.application.exception

class MarketDataNotFoundException(instrumentId: Int) :
    NotFoundException("No market data available for instrument $instrumentId")
