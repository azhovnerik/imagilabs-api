package com.anahoret.imagilabsapi.studentclassroomlink.domain

import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.studentclassroomlink.storage.StudentClassroomLinkEntity
import com.anahoret.imagilabsapi.studentclassroomlink.storage.StudentClassroomLinkEntityRepository
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import org.springframework.stereotype.Service
import java.util.*

interface StudentClassroomLinkService {

    fun link(classroom: Classroom, students: List<StudentProfile>)
    fun getStudentCounts(classroomIds: Iterable<UUID>): Map<UUID, Long>

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

    override fun getStudentCounts(classroomIds: Iterable<UUID>): Map<UUID, Long> {
        return studentClassroomLinkEntityRepository.getStudentCounts(classroomIds)
            .associate { it.classroomId to it.studentsCount }
    }
}
