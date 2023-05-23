package com.anahoret.imagilabsapi.signup.domain

import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator
import org.springframework.stereotype.Component

interface ImagiLabsEmailValidator {

    fun isValid(email: String): Boolean
}

@Component
class ImagiLabsEmailValidatorImpl : ImagiLabsEmailValidator {

    private val emailValidator = EmailValidator()

    override fun isValid(email: String): Boolean {
        if (email.isBlank()) return false
        if (!emailValidator.isValid(email, null)) return false
        if (!email.contains('@')) return false
        return email.substring(email.indexOf('@')).contains('.')
    }
}
