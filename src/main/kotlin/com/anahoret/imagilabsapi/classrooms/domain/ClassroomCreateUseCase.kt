package com.anahoret.imagilabsapi.classrooms.domain

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.studentclassroomlink.domain.StudentClassroomLinkService
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import org.springframework.stereotype.Service

interface ClassroomCreateUseCase {

    fun create(classroomCreateRequest: ClassroomCreateRequest): Either<List<ValidationError>, Classroom>
}

@Service
class ClassroomCreateUseCaseImpl(
    private val classroomCreateRequestValidator: ClassroomCreateRequestValidator,
    private val classroomService: ClassroomService,
    private val studentProfileService: StudentProfileService,
    private val studentClassroomLinkService: StudentClassroomLinkService
) : ClassroomCreateUseCase {

    override fun create(classroomCreateRequest: ClassroomCreateRequest): Either<List<ValidationError>, Classroom> {
        return classroomCreateRequestValidator.validate(classroomCreateRequest)
            .map {
                val classroom = classroomService.create(classroomCreateRequest)
                val students = studentProfileService.createStudents(classroomCreateRequest.studentCreateRequests)
                studentClassroomLinkService.link(classroom, students)
                classroom
            }
    }
}
