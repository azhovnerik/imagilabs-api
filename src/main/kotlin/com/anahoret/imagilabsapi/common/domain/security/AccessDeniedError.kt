package com.anahoret.imagilabsapi.common.domain.security

import com.anahoret.imagilabsapi.common.domain.error.OperationError

class AccessDeniedError(val message: String) : OperationError
