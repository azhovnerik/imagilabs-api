package com.anahoret.imagilabsapi.common.web

import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class ExceptionMappingControllerAdvice(
    private val logger: Logger
) {

    @ResponseBody
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Throwable::class)
    fun throwableHandler(t: Throwable): ErrorResponseDto<Void> {
        logger.error("Unhandled error caught", t)
        return ErrorResponseDto(
            code = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = t.message ?: "UNKNOWN_ERROR"
        )
    }

}
