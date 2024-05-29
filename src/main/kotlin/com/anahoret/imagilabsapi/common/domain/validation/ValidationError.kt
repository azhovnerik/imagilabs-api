package com.anahoret.imagilabsapi.common.domain.validation

import com.anahoret.imagilabsapi.common.domain.error.OperationError

open class ValidationError(val message: String) : OperationError {
    class FieldIsTooLong(field: String) : ValidationError("${field.uppercase()}_IS_TOO_LONG")
    class FieldIsBlank(field: String) : ValidationError("${field.uppercase()}_IS_BLANK")
    class FieldFormatInvalid(field: String) : ValidationError("${field.uppercase()}_FORMAT_IS_INVALID")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ValidationError

        return message == other.message
    }

    override fun hashCode(): Int {
        return message.hashCode()
    }

}

data class ValidationErrors(val errors: List<ValidationError>) : OperationError
