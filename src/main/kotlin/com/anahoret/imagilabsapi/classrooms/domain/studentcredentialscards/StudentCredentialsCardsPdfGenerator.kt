package com.anahoret.imagilabsapi.classrooms.domain.studentcredentialscards

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomAccessService
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import org.springframework.stereotype.Service
import java.io.InputStream
import java.util.*

interface StudentCredentialsCardsPdfGenerator {

    fun generate(generateBy: UserProfile, classroomId: UUID): Either<OperationError, StudentCredentialsCardsFile>
}

@Service
class StudentCredentialsCardsPdfGeneratorImpl(
    private val classroomService: ClassroomService,
    private val classroomAccessService: ClassroomAccessService
) : StudentCredentialsCardsPdfGenerator {

    override fun generate(
        generateBy: UserProfile,
        classroomId: UUID
    ): Either<OperationError, StudentCredentialsCardsFile> {
        val classroom = classroomService.getById(classroomId) ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()
        if (!classroomAccessService.canListStudentCredentials(generateBy, classroom))
            return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()
        return StudentCredentialsCardsFile(
            generatePdf(classroom),
            "${classroom.name}-students.pdf",
            StudentsCredentialsCardsFormat.PDF
        ).right()
    }

    private fun generatePdf(classroom: Classroom): InputStream {
        TODO("not implemented")
    }
}
