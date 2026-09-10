package com.cocos.challenge.api.exception

import com.cocos.challenge.application.exception.NotFoundException
import com.cocos.challenge.application.exception.ValidationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.OffsetDateTime

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val timestamp: OffsetDateTime = OffsetDateTime.now()
)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex: NotFoundException): ResponseEntity<ErrorResponse> =
        error(HttpStatus.NOT_FOUND, ex.message ?: "Resource not found")

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException): ResponseEntity<ErrorResponse> =
        error(HttpStatus.UNPROCESSABLE_ENTITY, ex.message ?: "Invalid operation")

    @ExceptionHandler(MissingIdempotencyKeyException::class)
    fun handleMissingIdempotencyKey(ex: MissingIdempotencyKeyException): ResponseEntity<ErrorResponse> =
        error(HttpStatus.BAD_REQUEST, ex.message ?: "Idempotency-Key header is required")

    @ExceptionHandler(MissingRequestHeaderException::class)
    fun handleMissingHeader(ex: MissingRequestHeaderException): ResponseEntity<ErrorResponse> =
        error(HttpStatus.BAD_REQUEST, "Required header '${ex.headerName}' is missing")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleRequestValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val fieldErrors = ex.bindingResult.fieldErrors.joinToString("; ") {
            "${it.field}: ${it.defaultMessage}"
        }
        val globalErrors = ex.bindingResult.globalErrors.joinToString("; ") {
            it.defaultMessage ?: it.code.orEmpty()
        }
        val message = listOf(fieldErrors, globalErrors)
            .filter { it.isNotBlank() }
            .joinToString("; ")
            .ifBlank { "Validation failed" }
        return error(HttpStatus.BAD_REQUEST, message)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadable(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val root = ex.mostSpecificCause.message ?: "Malformed request body"
        return error(HttpStatus.BAD_REQUEST, root.substringBefore('\n'))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        val allowed = ex.requiredType?.enumConstants?.joinToString()
        val message = if (allowed != null) {
            "Invalid value '${ex.value}' for '${ex.name}'. Allowed values: $allowed"
        } else {
            "Invalid value '${ex.value}' for '${ex.name}'"
        }
        return error(HttpStatus.BAD_REQUEST, message)
    }

    private fun error(status: HttpStatus, message: String): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(status).body(
            ErrorResponse(status = status.value(), error = status.reasonPhrase, message = message)
        )
}
