package com.anahoret.imagilabsapi.classrooms.domain.studentcredentialscards

import arrow.core.Either
import arrow.core.left
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomAccessService
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.classrooms.domain.DownloadStudentsCredentialsRequest
import com.anahoret.imagilabsapi.classrooms.domain.ListStudentsInClassroomUseCase
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service
import java.util.*

interface StudentCredentialsCardsGenerator {

    @Deprecated("Use download request version")
    fun generate(
        generateBy: TeacherProfile,
        classroomId: UUID,
        format: StudentsCredentialsCardsFormat
    ): Either<OperationError, StudentCredentialsCardsFile>

    fun generate(
        generateBy: TeacherProfile,
        classroomId: UUID,
        downloadRequest: DownloadStudentsCredentialsRequest
    ): Either<OperationError, StudentCredentialsCardsFile>
}

@Service
class StudentCredentialsCardsGeneratorImpl(
    private val classroomService: ClassroomService,
    private val classroomAccessService: ClassroomAccessService,
    private val studentCredentialsCardsPdfGenerator: StudentCredentialsCardsPdfGenerator,
    private val studentCredentialsCardsCsvGenerator: StudentCredentialsCardsCsvGenerator,
    private val listStudentsInClassroomUseCase: ListStudentsInClassroomUseCase,
    private val studentProfileService: StudentProfileService
) : StudentCredentialsCardsGenerator {

    override fun generate(
        generateBy: TeacherProfile,
        classroomId: UUID,
        format: StudentsCredentialsCardsFormat
    ): Either<OperationError, StudentCredentialsCardsFile> {
        val selectedStudents = studentProfileService.listByClassroom(classroomId)
            .map(StudentProfile::id)
            .toSet()
        val downloadRequest = DownloadStudentsCredentialsRequest(
            format = format,
            studentIds = selectedStudents
        )
        return doGenerate(generateBy, classroomId, downloadRequest)
    }

    override fun generate(
        generateBy: TeacherProfile,
        classroomId: UUID,
        downloadRequest: DownloadStudentsCredentialsRequest
    ): Either<OperationError, StudentCredentialsCardsFile> {
        return doGenerate(generateBy, classroomId, downloadRequest)
    }

    private fun doGenerate(
        generateBy: TeacherProfile,
        classroomId: UUID,
        downloadRequest: DownloadStudentsCredentialsRequest
    ): Either<OperationError, StudentCredentialsCardsFile> {
        val format = downloadRequest.format
        val classroom = classroomService.getById(classroomId)
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()
        if (!classroomAccessService.canListStudentCredentials(generateBy, classroom))
            return AccessDeniedError("ACCESS_TO_CLASSROOM_DENIED").left()

        val generator = when (format) {
            StudentsCredentialsCardsFormat.PDF -> studentCredentialsCardsPdfGenerator::generate
            StudentsCredentialsCardsFormat.CSV -> studentCredentialsCardsCsvGenerator::generate
        }

        return listStudentsInClassroomUseCase.list(generateBy, classroomId)
            .map { allStudentCards ->
                val selectedCards = downloadRequest.studentIds?.let {
                    allStudentCards.filter { it.id in downloadRequest.studentIds }
                } ?: allStudentCards

                val inputStream = generator(selectedCards)
                StudentCredentialsCardsFile(
                    inputStream = inputStream,
                    fileName = "${classroom.name}-students.${format.name.lowercase()}",
                    format = format
                )
            }
    }

}
