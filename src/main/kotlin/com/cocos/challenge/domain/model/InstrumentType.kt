package com.cocos.challenge.domain.model

enum class InstrumentType(val isCash: Boolean, val displayName: String) {
    CURRENCY(isCash = true,  displayName = "MONEDA"),
    STOCK   (isCash = false, displayName = "ACCIONES")
}
