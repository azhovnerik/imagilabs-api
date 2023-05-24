package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntity
import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntityRepository
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareService
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

interface ClassroomService {

    fun create(teacherId: UUID, classroomCreateRequest: ClassroomCreateRequest): Classroom
    fun countByTeacher(teacherId: UUID): Long
    fun listByTeacher(teacherId: UUID): List<Classroom>
    fun listByIds(classroomIds: Collection<UUID>): List<Classroom>
    fun getById(classroomId: UUID): Classroom?
    fun isClassroomOwnedByTeacher(classroomId: UUID, teacherId: UUID): Boolean
    fun getByAccessCode(accessCode: String): Classroom?
    fun delete(classroomId: UUID)
    fun update(classroomId: UUID, classroomUpdateRequest: ClassroomUpdateRequest): Classroom?
    fun listIdsByTeacher(teacherId: UUID): Set<UUID>
    fun getAllClassroomAsCoTeacher(classroomIds: List<UUID>): List<Classroom>
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

    override fun update(classroomId: UUID, classroomUpdateRequest: ClassroomUpdateRequest): Classroom? {
        return classroomEntityRepository.findByIdOrNull(classroomId)?.let {
            it.name = classroomUpdateRequest.name
            classroomEntityRepository.save(it)
            classroomId
        }?.let(::doGetClassroomById)
    }

    override fun countByTeacher(teacherId: UUID): Long {
        return classroomEntityRepository.countByTeacherId(teacherId)
    }

    override fun listByTeacher(teacherId: UUID): List<Classroom> {
        return mapToClassrooms(classroomEntityRepository.findAllByTeacherId(teacherId))
    }

    override fun getAllClassroomAsCoTeacher(classroomIds: List<UUID>): List<Classroom> {
        return mapToClassrooms(classroomEntityRepository.findAllById(classroomIds))
            .toCoTeacherClassrooms()
    }

    override fun listByIds(classroomIds: Collection<UUID>): List<Classroom> {
        return mapToClassrooms(classroomEntityRepository.findAllById(classroomIds))
    }

    override fun getById(classroomId: UUID): Classroom? {
        return doGetClassroomById(classroomId)
    }

    override fun getByAccessCode(accessCode: String): Classroom? {
        return classroomEntityRepository.findByAccessCode(accessCode)
            ?.let {
                val studentsCount = studentClassroomLinkService.getStudentCount(it.id!!)
                val projectsCount = projectClassroomShareService.getProjectCount(it.id!!)
                Classroom.fromEntity(it, studentsCount, projectsCount)
            }
    }

    override fun isClassroomOwnedByTeacher(classroomId: UUID, teacherId: UUID): Boolean {
        return classroomEntityRepository.existsByIdAndTeacherId(classroomId, teacherId)
    }

    override fun delete(classroomId: UUID) {
        classroomEntityRepository.deleteById(classroomId)
    }

    override fun listIdsByTeacher(teacherId: UUID): Set<UUID> {
        return classroomEntityRepository.findAllByTeacherId(teacherId)
            .map { it.id!! }
            .toSet()
    }

    private fun doGetClassroomById(classroomId: UUID): Classroom? {
        return classroomEntityRepository.findByIdOrNull(classroomId)
            ?.let {
                val studentsCount = studentClassroomLinkService.getStudentCount(it.id!!)
                val projectsCount = projectClassroomShareService.getProjectCount(it.id!!)
                Classroom.fromEntity(it, studentsCount, projectsCount)
            }
    }

    private fun mapToClassrooms(classroomEntities: Iterable<ClassroomEntity>): List<Classroom> {
        val classroomIds = classroomEntities.map { it.id!! }
        val studentCounts = studentClassroomLinkService.getStudentCounts(classroomIds)
        val projectCounts = projectClassroomShareService.getProjectCountsByClassrooms(classroomIds)
        return classroomEntities
            .map {
                Classroom.fromEntity(
                    classroomEntity = it,
                    studentCounts.getOrDefault(it.id!!, 0),
                    projectCounts.getOrDefault(it.id!!, 0)
                )
            }
    }

    private fun generateUniqueAccessCode(): String {
        for (i in 1..100) {
            val accessCode = RandomStringUtils.randomAlphabetic(6).uppercase()
            if (classroomEntityRepository.findByAccessCode(accessCode) == null) {
                return accessCode
            }
        }
        throw RuntimeException("EXCEEDED_NUMBER_OF_ATTEMPTS_TO_GENERATE_UNIQUE_CODE_FOR_CLASS")
    }

    fun List<Classroom>.toCoTeacherClassrooms(): List<Classroom> {
        return this.map { it.teacherRole = TeacherRole.CO_TEACHER; it }
    }
}
