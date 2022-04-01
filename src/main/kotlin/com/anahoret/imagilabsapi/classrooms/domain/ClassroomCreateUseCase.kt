package com.anahoret.imagilabsapi.classrooms.domain

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

interface ClassroomCreateUseCase {

    fun create(
        teacherProfile: TeacherProfile,
        classroomCreateRequest: ClassroomCreateRequest
    ): Either<List<ValidationError>, Classroom>
}

@Service
class ClassroomCreateUseCaseImpl(
    private val classroomValidator: ClassroomValidator,
    private val classroomService: ClassroomService,
    private val studentProfileService: StudentProfileService
) : ClassroomCreateUseCase {

    @Transactional(rollbackOn = [Throwable::class])
    override fun create(
        teacherProfile: TeacherProfile,
        classroomCreateRequest: ClassroomCreateRequest
    ): Either<List<ValidationError>, Classroom> {
        return classroomValidator.validate(classroomCreateRequest)
            .flatMap { checkTeacherClassesMaxCount(teacherProfile) }
            .map { doCreateClassroom(teacherProfile.id, classroomCreateRequest) }
    }

    private fun doCreateClassroom(teacherId: UUID, classroomCreateRequest: ClassroomCreateRequest): Classroom {
        val classroom = classroomService.create(teacherId, classroomCreateRequest)
        studentProfileService.createStudents(classroom.id, classroomCreateRequest.studentCreateRequests)
        return classroom
    }

    private fun checkTeacherClassesMaxCount(teacherProfile: TeacherProfile): Either<List<ValidationError>, Unit> {
        return if (classroomService.countByTeacher(teacherProfile.id) >= 10) {
            listOf(ValidationError("TEACHER_CLASSES_COUNT_LIMIT_EXCEEDED")).left()
        } else {
            Unit.right()
        }
    }
}
