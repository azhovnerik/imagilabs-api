package com.anahoret.imagilabsapi.teacherchecklist.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import org.springframework.stereotype.Service
import java.util.*

interface GetTeacherCheckListUseCase {

    fun get(teacherId: UUID): Either<OperationError, TeacherCheckList>
}

@Service
class GetTeacherCheckListUseCaseImpl(
    private val teacherProfileService: TeacherProfileService,
    private val teacherCheckListService: TeacherCheckListService,
) : GetTeacherCheckListUseCase {

    override fun get(teacherId: UUID): Either<OperationError, TeacherCheckList> {

        if (!teacherProfileService.exists(teacherId))
            return NotFoundError("TEACHER_NOT_FOUND").left()

        return teacherCheckListService.getCheckList(teacherId).right()
    }
}
