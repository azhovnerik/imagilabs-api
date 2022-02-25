package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntity
import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntityRepository
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareService
import com.anahoret.imagilabsapi.studentclassroomlink.domain.StudentClassroomLinkService
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

interface ClassroomService {

    fun create(teacherId: UUID, classroomCreateRequest: ClassroomCreateRequest): Classroom
    fun countByTeacher(teacherId: UUID): Long
    fun listByTeacher(teacherId: UUID): List<Classroom>
    fun getById(classroomId: UUID): Classroom?
    fun isClassroomOwnedByTeacher(classroomId: UUID, teacherId: UUID): Boolean
}

@Service
class ClassroomServiceImpl(
    private val classroomEntityRepository: ClassroomEntityRepository,
    private val studentClassroomLinkService: StudentClassroomLinkService,
    private val projectClassroomShareService: ProjectClassroomShareService
) : ClassroomService {

    override fun create(teacherId: UUID, classroomCreateRequest: ClassroomCreateRequest): Classroom {
        val accessCode = generateUniqueAccessCode()
        return classroomEntityRepository.save(
            ClassroomEntity(
                classroomCreateRequest.name,
                accessCode,
                teacherId
            )
        ).let { Classroom.fromEntity(it, studentsCount = 0, projectsCount = 0) }
    }

    override fun countByTeacher(teacherId: UUID): Long {
        return classroomEntityRepository.countByTeacherId(teacherId)
    }

    override fun listByTeacher(teacherId: UUID): List<Classroom> {
        val classroomEntities = classroomEntityRepository.findAllByTeacherId(teacherId)
        val classroomIds = classroomEntities.map { it.id!! }
        val studentCounts = studentClassroomLinkService.getStudentCounts(classroomIds)
        val projectCounts = projectClassroomShareService.getProjectCounts(classroomIds)
        return classroomEntities
            .map {
                Classroom.fromEntity(
                    classroomEntity = it,
                    studentCounts.getOrDefault(it.id!!, 0),
                    projectCounts.getOrDefault(it.id!!, 0)
                )
            }
    }

    override fun getById(classroomId: UUID): Classroom? {
        return classroomEntityRepository.findByIdOrNull(classroomId)
            ?.let {
                val studentsCount = studentClassroomLinkService.getStudentCount(it.id!!)
                val projectsCount = projectClassroomShareService.getProjectCount(it.id!!)
                Classroom.fromEntity(it, studentsCount, projectsCount)
            }
    }

    override fun isClassroomOwnedByTeacher(classroomId: UUID, teacherId: UUID): Boolean {
        return classroomEntityRepository.existsByIdAndTeacherId(classroomId, teacherId)
    }

    private fun generateUniqueAccessCode(): String {
        val accessCode = RandomStringUtils.randomAlphanumeric(6)
        for (i in 1..100) {
            if (classroomEntityRepository.findByAccessCode(accessCode) == null) {
                return accessCode
            }
        }
        throw RuntimeException("EXCEEDED_NUMBER_OF_ATTEMPTS_TO_GENERATE_UNIQUE_CODE_FOR_CLASS")
    }
}
