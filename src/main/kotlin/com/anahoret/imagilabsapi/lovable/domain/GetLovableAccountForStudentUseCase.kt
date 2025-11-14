package com.anahoret.imagilabsapi.lovable.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomAccessService
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import org.springframework.stereotype.Service
import java.util.*

interface GetLovableAccountForStudentUseCase {
    fun get(userProfile: UserProfile, studentId: UUID): Either<OperationError, LovableAccount>
}

@Service
class GetLovableAccountForStudentUseCaseImpl(
    private val classroomAccessService: ClassroomAccessService,
    private val lovableAccountService: LovableAccountService,
    private val studentProfileService: StudentProfileService,
    private val studentClassroomLinkService: StudentClassroomLinkService
) : GetLovableAccountForStudentUseCase {

    override fun get(userProfile: UserProfile, studentId: UUID): Either<OperationError, LovableAccount> {
        val student = studentProfileService.getStudentById(studentId)
            ?: return NotFoundError("STUDENT_NOT_FOUND").left()

        val classrooms = studentClassroomLinkService.listClassroomsByStudent(student.id)
            .takeIf { it.isNotEmpty() }
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

        if (classrooms.none { classroomAccessService.canListStudentCredentials(userProfile, it) })
            return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()

        val lovableAccount = lovableAccountService.getActive(student)
            ?: return NotFoundError("LOVABLE_ACCOUNT_NOT_FOUND").left()

        return lovableAccount.right()
    }
}
