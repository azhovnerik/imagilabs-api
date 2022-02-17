package com.anahoret.imagilabsapi.web

open class ResponseDto<T>(
    val success: Boolean,
    val payload: T? = null,
    val errors: List<ResponseErrorMessageDto> = emptyList()
)

open class SuccessResponseDto<T>(
    payload: T? = null,
    errors: List<ResponseErrorMessageDto> = emptyList()
) : ResponseDto<T>(success = true, payload, errors)

object EmptySuccessResponseDto : SuccessResponseDto<Void>()

class ErrorResponseDto<T>(
    errors: List<ResponseErrorMessageDto> = emptyList()
) : ResponseDto<T>(success = false, payload = null, errors) {

    constructor(code: Int, message: String) : this(listOf(ResponseErrorMessageDto(code, message)))
}

class ResponseErrorMessageDto(
    val code: Int,
    val message: String
)
