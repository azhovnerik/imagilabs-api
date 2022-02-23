package com.anahoret.imagilabsapi.common.domain.validation

import arrow.core.Either
import arrow.core.left
import arrow.core.right

abstract class AbstractValidator<T> {

    protected fun validate(doValidate: (MutableList<ValidationError>) -> Unit): Either<List<ValidationError>, Unit> {
        val errors = mutableListOf<ValidationError>()
        doValidate(errors)
        return errors.takeIf { it.isNotEmpty() }?.left() ?: Unit.right()
    }

    protected fun rejectIfBlank(value: String, errors: MutableList<ValidationError>, field: String) {
        if (value.isBlank()) errors.add(ValidationError.FieldIsBlank(field))
    }

    protected fun rejectIfTooLong(value: String, maxLength: Int, errors: MutableList<ValidationError>, field: String) {
        if (value.length > maxLength) errors.add(ValidationError.FieldIsTooLong(field))
    }

}
