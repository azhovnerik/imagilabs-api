package com.anahoret.imagilabsapi.lovable.domain.accountcards

import arrow.core.Either
import arrow.core.left
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomAccessService
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.lovable.domain.DownloadStudentsLovableAccountsRequest
import com.anahoret.imagilabsapi.lovable.domain.GetLovableCredentialsForClassroomUseCase
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service
import java.util.*

interface StudentLovableAccountCardsGenerator {
    fun generate(
        generateBy: TeacherProfile,
        classroomId: UUID,
        downloadRequest: DownloadStudentsLovableAccountsRequest
    ): Either<OperationError, StudentLovableAccountCardsFile>
}

@Service
class StudentLovableAccountCardsGeneratorImpl(
    private val classroomService: ClassroomService,
    private val classroomAccessService: ClassroomAccessService,
    private val getLovableCredentialsForClassroomUseCase: GetLovableCredentialsForClassroomUseCase,
    private val studentLovableAccountCardsPdfGenerator: StudentLovableAccountCardsPdfGenerator,
    private val studentLovableAccountCardsCsvGenerator: StudentLovableAccountCardsCsvGenerator
) : StudentLovableAccountCardsGenerator {

    override fun generate(
        generateBy: TeacherProfile,
        classroomId: UUID,
        downloadRequest: DownloadStudentsLovableAccountsRequest
    ): Either<OperationError, StudentLovableAccountCardsFile> {
        return doGenerate(generateBy, classroomId, downloadRequest)
    }

    private fun doGenerate(
        generateBy: TeacherProfile,
        classroomId: UUID,
        downloadRequest: DownloadStudentsLovableAccountsRequest
    ): Either<OperationError, StudentLovableAccountCardsFile> {
        val format = downloadRequest.format
        val classroom = classroomService.getById(classroomId)
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()
        if (!classroomAccessService.canListStudentCredentials(generateBy, classroom))
            return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()

        val generator = when (format) {
            StudentsLovableAccountCardsFormat.PDF -> studentLovableAccountCardsPdfGenerator::generate
            StudentsLovableAccountCardsFormat.CSV -> studentLovableAccountCardsCsvGenerator::generate
        }

        return getLovableCredentialsForClassroomUseCase.getCredentials(generateBy, classroomId)
            .map { allStudentCards ->
                val selectedCards = downloadRequest.studentIds?.let {
                    allStudentCards.filter { it.connectedUserId in downloadRequest.studentIds }
                } ?: allStudentCards

                val inputStream = generator(selectedCards)
                StudentLovableAccountCardsFile(
                    inputStream = inputStream,
                    fileName = "lovable-${classroom.name}-students.${format.name.lowercase()}",
                    format = format
                )
            }
    }
}
