package com.anahoret.imagilabsapi.edlink.domain

import arrow.core.Either
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import org.springframework.stereotype.Service
import java.util.*

interface EdLinkSubjectsService {
    fun getTeacherSubjects(token: String, personId: UUID): Either<OperationError, String?>
}

@Service
class EdLinkSubjectsServiceImpl : EdLinkSubjectsService {
    override fun getTeacherSubjects(token: String, personId: UUID): Either<OperationError, String?> = null.right()
}
