package com.anahoret.imagilabsapi.studentclassroomlink.domain

import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.studentclassroomlink.storage.StudentClassroomLinkEntity
import com.anahoret.imagilabsapi.studentclassroomlink.storage.StudentClassroomLinkEntityRepository
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import org.springframework.stereotype.Service

interface StudentClassroomLinkService {

    fun link(classroom: Classroom, students: List<StudentProfile>)

}

@Service
class StudentClassroomLinkServiceImpl(
    private val studentClassroomLinkEntityRepository: StudentClassroomLinkEntityRepository
) : StudentClassroomLinkService {

    override fun link(classroom: Classroom, students: List<StudentProfile>) {
        students.map { student ->
            StudentClassroomLinkEntity(student.id, classroom.id)
        }.let(studentClassroomLinkEntityRepository::saveAll)
    }
}
