package com.anahoret.imagilabsapi.common.domain.validation

import com.anahoret.imagilabsapi.common.domain.error.OperationError

open class ValidationError(val message: String) : OperationError {
    class FieldIsTooLong(field: String) : ValidationError("${field.uppercase()}_IS_TOO_LONG")
    class FieldIsBlank(field: String) : ValidationError("${field.uppercase()}_IS_TOO_LONG")
    class FieldFormatInvalid(field: String) : ValidationError("${field.uppercase()}_FORMAT_IS_INVALID")
}
