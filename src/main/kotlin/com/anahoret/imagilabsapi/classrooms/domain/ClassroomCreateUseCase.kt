package com.anahoret.imagilabsapi.classrooms.domain

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.studentclassroomlink.domain.StudentClassroomLinkService
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service

interface ClassroomCreateUseCase {

    fun create(
        teacherProfile: TeacherProfile,
        classroomCreateRequest: ClassroomCreateRequest
    ): Either<List<ValidationError>, Classroom>
}

@Service
class ClassroomCreateUseCaseImpl(
    private val classroomCreateRequestValidator: ClassroomCreateRequestValidator,
    private val classroomService: ClassroomService,
    private val studentProfileService: StudentProfileService,
    private val studentClassroomLinkService: StudentClassroomLinkService
) : ClassroomCreateUseCase {

    override fun create(
        teacherProfile: TeacherProfile,
        classroomCreateRequest: ClassroomCreateRequest
    ): Either<List<ValidationError>, Classroom> {
        return classroomCreateRequestValidator.validate(classroomCreateRequest)
            .flatMap { checkTeacherClassesMaxCount(teacherProfile) }
            .map { doCreateClassroom(classroomCreateRequest) }
    }

    private fun doCreateClassroom(classroomCreateRequest: ClassroomCreateRequest): Classroom {
        val classroom = classroomService.create(classroomCreateRequest)
        val students = studentProfileService.createStudents(classroomCreateRequest.studentCreateRequests)
        studentClassroomLinkService.link(classroom, students)
        return classroom
    }

    private fun checkTeacherClassesMaxCount(teacherProfile: TeacherProfile): Either<List<ValidationError>, Unit> {
        return if (classroomService.countByTeacher(teacherProfile.id) > 10) {
            listOf(ValidationError("TEACHER_CLASSES_COUNT_LIMIT_EXCEEDED")).left()
        } else {
            Unit.right()
        }
    }
}
