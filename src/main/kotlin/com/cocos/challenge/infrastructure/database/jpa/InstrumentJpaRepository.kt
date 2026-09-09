package com.cocos.challenge.infrastructure.database.jpa

import com.cocos.challenge.infrastructure.database.entity.InstrumentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface InstrumentJpaRepository :
    JpaRepository<InstrumentEntity, Int>,
    JpaSpecificationExecutor<InstrumentEntity> {

    fun findByTickerIgnoreCase(ticker: String): InstrumentEntity?
}
