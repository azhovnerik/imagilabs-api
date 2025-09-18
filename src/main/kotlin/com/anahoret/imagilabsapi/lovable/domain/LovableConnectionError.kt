package com.anahoret.imagilabsapi.lovable.domain

import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError

interface LovableConnectionError : OperationError {
    val message: String
}

class MaxNumberOfConnectedAccountsExceededError
    : LovableConnectionError, ValidationError("Max number of connected accounts exceeded")

class OutOfLovableAccountsError : LovableConnectionError, ValidationError("Out of Lovable accounts")

class UnapplicableUserTypeError : LovableConnectionError, ValidationError("Cannot connect Lovable account to user")
