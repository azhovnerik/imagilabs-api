package com.anahoret.imagilabsapi.userclassroomlink.domain

import com.anahoret.imagilabsapi.students.storage.StudentProfileEntityRepository
import com.anahoret.imagilabsapi.userclassroomlink.storage.StudentClassroomLinkRepository
import org.springframework.stereotype.Service
import java.util.*

interface StudentClassroomLinkService {

    fun getStudentCounts(classroomIds: Iterable<UUID>): Map<UUID, Long>
    fun getStudentCount(classroomId: UUID): Long
}

@Service
class StudentClassroomLinkServiceImpl(
    private val studentClassroomLinkRepository: StudentClassroomLinkRepository,
    private val studentProfileEntityRepository: StudentProfileEntityRepository
) : StudentClassroomLinkService {

    override fun getStudentCounts(classroomIds: Iterable<UUID>): Map<UUID, Long> {
        return studentClassroomLinkRepository.getStudentCounts(classroomIds)
            .associate { it.classroomId to it.studentsCount }
    }

    override fun getStudentCount(classroomId: UUID): Long {
        return studentProfileEntityRepository.countByClassroomId(classroomId)
    }

}
