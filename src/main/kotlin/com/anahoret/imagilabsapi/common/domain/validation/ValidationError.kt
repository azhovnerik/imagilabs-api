package com.anahoret.imagilabsapi.common.domain.validation

open class ValidationError(val message: String) {
    class FieldIsTooLong(field: String) : ValidationError("${field.uppercase()}_IS_TOO_LONG")
    class FieldIsBlank(field: String) : ValidationError("${field.uppercase()}_IS_TOO_LONG")
    class FieldFormatInvalid(field: String) : ValidationError("${field.uppercase()}_FORMAT_IS_INVALID")
}
