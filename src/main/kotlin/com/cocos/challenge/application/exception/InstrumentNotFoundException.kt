package com.cocos.challenge.application.exception

class InstrumentNotFoundException(identifier: String) :
    NotFoundException("Instrument '$identifier' not found")
