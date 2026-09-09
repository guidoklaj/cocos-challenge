package com.cocos.challenge.api.request

import com.cocos.challenge.domain.model.OrderSide
import com.cocos.challenge.domain.model.OrderType
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

/**
 * Payload accepted by `POST /orders`.
 *
 * Users may specify either the number of shares (`size`) or an investment amount in ARS
 * (`amount`). Exactly one of the two must be provided; when `amount` is used the use case
 * computes the maximum whole number of shares that fit into it at the order price.
 *
 * For MARKET orders the `price` field is ignored — the latest market close is used.
 * For CASH_IN / CASH_OUT orders the instrument fields are ignored — the cash instrument
 * (`ARS`) is resolved by the use case.
 */
data class SubmitOrderRequest(
    @field:NotNull
    val userId: Int,

    val instrumentId: Int?,

    val ticker: String?,

    @field:NotNull
    val side: OrderSide,

    @field:NotNull
    val type: OrderType,

    @field:Positive
    val size: Int?,

    @field:Positive
    val amount: BigDecimal?,

    @field:Positive
    val price: BigDecimal?
) {

    @AssertTrue(message = "Exactly one of 'size' or 'amount' must be provided")
    fun isSizeXorAmount(): Boolean = (size == null) != (amount == null)

    @AssertTrue(message = "'instrumentId' or 'ticker' is required for non-cash orders")
    fun isInstrumentPresentWhenNeeded(): Boolean =
        side?.isCashMovement == true || instrumentId != null || !ticker.isNullOrBlank()

    @AssertTrue(message = "'price' is required for LIMIT orders")
    fun isPricePresentForLimit(): Boolean =
        type != OrderType.LIMIT || price != null
}
