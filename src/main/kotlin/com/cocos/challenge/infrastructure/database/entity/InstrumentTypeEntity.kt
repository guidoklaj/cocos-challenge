package com.cocos.challenge.infrastructure.database.entity

import com.cocos.challenge.domain.model.InstrumentType

enum class InstrumentTypeEntity(val domain: InstrumentType) {
    CURRENCY(InstrumentType.CURRENCY),
    STOCK(InstrumentType.STOCK);

    companion object {
        fun fromDomain(type: InstrumentType): InstrumentTypeEntity =
            entries.first { it.domain == type }
    }
}
