package com.anahoret.imagilabsapi.common.web

import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

fun <T> List<ValidationError>.toBadRequestResponse(): ResponseEntity<ResponseDto<T?>> {
    val responseErrors = this.map {
        ResponseErrorMessageDto(HttpStatus.BAD_REQUEST.value(), it.message)
    }
    return ResponseEntity.badRequest().body(ErrorResponseDto(responseErrors))
}
