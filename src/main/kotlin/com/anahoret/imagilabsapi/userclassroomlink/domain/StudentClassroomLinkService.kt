package com.anahoret.imagilabsapi.userclassroomlink.domain

import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.userclassroomlink.storage.StudentClassroomEntity
import com.anahoret.imagilabsapi.userclassroomlink.storage.StudentClassroomEntityRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*

interface StudentClassroomLinkService {
    fun listClassroomsByStudent(studentId: UUID): List<Classroom>
    fun addStudentToClassroom(studentId: UUID, classroomId: UUID)
    fun addStudentsToClassroom(studentIds: List<UUID>, classroomId: UUID)
    fun removeStudentFromClassroom(studentId: UUID, classroomId: UUID)
    fun listStudentsByClassroom(
        classroomId: UUID,
        searchQuery: String? = null,
        sort: Sort? = null
    ): List<StudentProfile>
    fun getStudentCounts(classroomIds: Iterable<UUID>): Map<UUID, Long>
    fun getStudentCount(classroomId: UUID): Long
    fun listClassroomIdsByStudent(studentId: UUID): List<UUID>
    fun deleteByStudentIds(studentIds: Collection<UUID>)
}

@Service
class StudentClassroomLinkServiceImpl(
    private val studentClassroomEntityRepository: StudentClassroomEntityRepository,
    private val classroomService: ClassroomService
) : StudentClassroomLinkService {

    override fun getStudentCounts(classroomIds: Iterable<UUID>): Map<UUID, Long> {
        return studentClassroomEntityRepository.getStudentCounts(classroomIds)
            .associate { it.classroomId to it.studentsCount }
    }

    override fun getStudentCount(classroomId: UUID): Long {
        return studentClassroomEntityRepository.countByClassroomId(classroomId)
    }

    override fun listClassroomsByStudent(studentId: UUID): List<Classroom> {
        return studentClassroomEntityRepository.findAllByStudentId(studentId)
            .map(StudentClassroomEntity::classroomId)
            .let(classroomService::listByIds)
            .filterNot(Classroom::blocked)
    }

    @Transactional
    override fun addStudentToClassroom(studentId: UUID, classroomId: UUID) {
        if (!studentClassroomEntityRepository.existsByStudentIdAndClassroomId(studentId, classroomId)) {
            studentClassroomEntityRepository.save(StudentClassroomEntity(studentId, classroomId))
        }
    }

    @Transactional
    override fun addStudentsToClassroom(studentIds: List<UUID>, classroomId: UUID) {
        val studentsAlreadyInClassroom = studentClassroomEntityRepository
            .findAllByClassroomId(classroomId)
            .map(StudentClassroomEntity::studentId)
            .toSet()
        val studentIdsToAdd = studentIds.filter { it !in studentsAlreadyInClassroom }
        studentIdsToAdd.map { StudentClassroomEntity(it, classroomId) }
            .let { studentClassroomEntityRepository.saveAll(it) }
    }

    @Transactional
    override fun removeStudentFromClassroom(studentId: UUID, classroomId: UUID) {
        studentClassroomEntityRepository.deleteByStudentIdAndClassroomId(studentId, classroomId)
    }

    override fun listStudentsByClassroom(classroomId: UUID, searchQuery: String?, sort: Sort?): List<StudentProfile> {
        return if (searchQuery == null) doListByClassroom(classroomId, sort ?: Sort.unsorted())
        else studentClassroomEntityRepository.findAllByClassroomId(classroomId, searchQuery, sort ?: Sort.unsorted())
            .map(StudentProfile.Companion::fromEntity)
    }

    override fun listClassroomIdsByStudent(studentId: UUID): List<UUID> {
        return studentClassroomEntityRepository.findAllClassroomIdsByStudentId(studentId)
    }

    @Transactional
    override fun deleteByStudentIds(studentIds: Collection<UUID>) {
        studentClassroomEntityRepository.deleteByStudentIdIn(studentIds)
    }

    private fun doListByClassroom(classroomId: UUID, sort: Sort): List<StudentProfile> {
        return studentClassroomEntityRepository.findAllByClassroomId(classroomId, sort)
            .map(StudentProfile.Companion::fromEntity)
    }

}
