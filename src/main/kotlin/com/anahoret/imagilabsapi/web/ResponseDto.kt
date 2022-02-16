package com.anahoret.imagilabsapi.web

open class ResponseDto<T>(
    val success: Boolean,
    val payload: T? = null,
    val errors: List<ResponseErrorDto> = emptyList()
)

open class SuccessResponseDto<T>(
    payload: T? = null,
    errors: List<ResponseErrorDto> = emptyList()
) : ResponseDto<T>(success = true, payload, errors)

object EmptySuccessResponseDto : SuccessResponseDto<Void>()

class ResponseErrorDto(
    val code: Int,
    val message: String
)
