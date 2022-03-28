package com.anahoret.imagilabsapi.userclassroomlink.domain

import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.userclassroomlink.storage.StudentClassroomLinkEntity
import com.anahoret.imagilabsapi.userclassroomlink.storage.StudentClassroomLinkEntityRepository
import org.springframework.stereotype.Service
import java.util.*

interface StudentClassroomLinkService {

    fun link(classroom: Classroom, students: List<StudentProfile>)
    fun unlinkFromAll(studentId: UUID)
    fun getStudentCounts(classroomIds: Iterable<UUID>): Map<UUID, Long>
    fun getStudentCount(classroomId: UUID): Long
    fun listByClassroom(classroomId: UUID): List<StudentClassroomLink>
    fun isStudentLinkedToClassroom(studentId: UUID, classroomId: UUID): Boolean
    fun getLinks(studentId: UUID): List<StudentClassroomLink>

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

    override fun unlinkFromAll(studentId: UUID) {
        studentClassroomLinkEntityRepository.deleteAllByStudentId(studentId)
    }

    override fun getStudentCounts(classroomIds: Iterable<UUID>): Map<UUID, Long> {
        return studentClassroomLinkEntityRepository.getStudentCounts(classroomIds)
            .associate { it.classroomId to it.studentsCount }
    }

    override fun getStudentCount(classroomId: UUID): Long {
        return studentClassroomLinkEntityRepository.countByClassroomId(classroomId)
    }

    override fun listByClassroom(classroomId: UUID): List<StudentClassroomLink> {
        return studentClassroomLinkEntityRepository.findAllByClassroomId(classroomId)
            .map(StudentClassroomLink.Companion::fromEntity)
    }

    override fun isStudentLinkedToClassroom(studentId: UUID, classroomId: UUID): Boolean {
        return studentClassroomLinkEntityRepository.existsByStudentIdAndClassroomId(studentId, classroomId)
    }

    override fun getLinks(studentId: UUID): List<StudentClassroomLink> {
        return studentClassroomLinkEntityRepository.findAllByStudentId(studentId)
            .map(StudentClassroomLink.Companion::fromEntity)
    }

}
