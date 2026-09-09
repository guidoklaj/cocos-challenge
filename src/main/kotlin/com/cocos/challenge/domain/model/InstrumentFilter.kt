package com.cocos.challenge.domain.model

data class InstrumentFilter(
    val ticker: String? = null,
    val name: String? = null,
    val type: InstrumentType? = null
)
