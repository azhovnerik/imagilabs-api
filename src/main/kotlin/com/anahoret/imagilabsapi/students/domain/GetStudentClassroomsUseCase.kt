package com.anahoret.imagilabsapi.students.domain

import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import org.springframework.stereotype.Service

interface GetStudentClassroomsUseCase {
    fun get(studentProfile: StudentProfile): List<Classroom>
}

@Service
class GetStudentClassroomsUseCaseImpl(
    private val studentClassroomLinkService: StudentClassroomLinkService
) : GetStudentClassroomsUseCase {

    override fun get(studentProfile: StudentProfile): List<Classroom> {
        return studentClassroomLinkService.listClassroomsByStudent(studentProfile.id)
    }

}
