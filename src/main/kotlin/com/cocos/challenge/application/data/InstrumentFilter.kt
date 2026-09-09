package com.cocos.challenge.application.data

import com.cocos.challenge.domain.model.InstrumentType

data class InstrumentFilter(
    val ticker: String? = null,
    val name: String? = null,
    val type: InstrumentType? = null
) {
    val isEmpty: Boolean
        get() = ticker.isNullOrBlank() && name.isNullOrBlank() && type == null
}