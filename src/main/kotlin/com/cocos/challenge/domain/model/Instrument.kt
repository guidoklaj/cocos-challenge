package com.cocos.challenge.domain.model

data class Instrument(
    val id: Int,
    val ticker: String,
    val name: String,
    val type: InstrumentType
) {
    fun isCash() = type.isCash
}
