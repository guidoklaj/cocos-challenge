package com.cocos.challenge.domain.model

enum class InstrumentType(val isCash: Boolean) {
    MONEDA(isCash = true),
    ACCIONES(isCash = false)
}
