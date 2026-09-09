package com.cocos.challenge.infrastructure.database.repository

import com.cocos.challenge.application.repository.InstrumentRepository
import com.cocos.challenge.domain.model.Instrument
import com.cocos.challenge.application.data.InstrumentFilter
import com.cocos.challenge.infrastructure.database.entity.InstrumentEntity
import com.cocos.challenge.infrastructure.database.jpa.InstrumentJpaRepository
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
class InstrumentRepositoryImpl(
    private val jpa: InstrumentJpaRepository
) : InstrumentRepository {

    override fun findById(id: Int): Instrument? =
        jpa.findById(id).getOrNull()?.toDomain()

    override fun findByTicker(ticker: String): Instrument? =
        jpa.findByTickerIgnoreCase(ticker)?.toDomain()

    override fun findByFilter(filter: InstrumentFilter): List<Instrument> =
        jpa.findAll(filter.toSpecification(), Sort.by("ticker")).map { it.toDomain() }

    private fun InstrumentFilter.toSpecification(): Specification<InstrumentEntity> =
        Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()
            ticker?.takeIf { it.isNotBlank() }?.let {
                predicates += cb.like(cb.lower(root.get("ticker")), "%${it.lowercase()}%")
            }
            name?.takeIf { it.isNotBlank() }?.let {
                predicates += cb.like(cb.lower(root.get("name")), "%${it.lowercase()}%")
            }
            type?.let {
                predicates += cb.equal(root.get<Any>("type"), it)
            }
            cb.and(*predicates.toTypedArray())
        }
}
