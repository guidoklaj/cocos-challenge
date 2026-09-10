package com.cocos.challenge.application.data

data class InstrumentFilter(
    val ticker: String? = null,
    val name: String? = null
) {
    val isEmpty: Boolean
        get() = ticker.isNullOrBlank() && name.isNullOrBlank()
}
