package com.anahoret.imagilabsapi.common.web

import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

fun <T> List<ValidationError>.toBadRequestResponse(): ResponseEntity<ResponseDto<T?>> {
    val responseErrors = this.map {
        ResponseErrorMessageDto(HttpStatus.BAD_REQUEST.value(), it.message)
    }
    return ResponseEntity.badRequest().body(ErrorResponseDto(responseErrors))
}

fun <T> NotFoundError.toNotFoundResponse(): ResponseEntity<ResponseDto<T>> {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponseDto(HttpStatus.NOT_FOUND.value(), this.message))
}

fun <T> AccessDeniedError.toForbiddenResponse(): ResponseEntity<ResponseDto<T>> {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ErrorResponseDto(HttpStatus.FORBIDDEN.value(), this.message))
}

fun <T> unknownErrorResponse(): ResponseEntity<ResponseDto<T>> {
    return ResponseEntity.internalServerError()
        .body(ErrorResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), "UNKNOWN_ERROR"))
}

fun <T> mapErrors(operationError: OperationError): ResponseEntity<ResponseDto<T?>> {
    return when (operationError) {
        is NotFoundError -> operationError.toNotFoundResponse()
        is AccessDeniedError -> operationError.toForbiddenResponse()
        else -> unknownErrorResponse()
    }
}
