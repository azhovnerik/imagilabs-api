package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntity
import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntityRepository
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareService
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionService
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Clock
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
    private val projectClassroomShareService: ProjectClassroomShareService,
    private val coTeacherService: CoTeacherService,
    private val teacherSubscriptionService: TeacherSubscriptionService,
    private val clock: Clock
) : ClassroomService {

    override fun create(teacherId: UUID, classroomCreateRequest: ClassroomCreateRequest): Classroom {
        val accessCode = generateUniqueAccessCode()
        return classroomEntityRepository.save(
            ClassroomEntity(
                classroomCreateRequest.name,
                accessCode,
                teacherId
            )
        ).let {
            val blocked = calculateBlockedForSingleClassroom(it)
            Classroom.fromEntity(it, studentsCount = 0, projectsCount = 0, coTeachersCount = 0, blocked = blocked)
        }
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
        return mapToClassrooms(classroomEntityRepository.findAllById(classroomIds), isCoTeacher = true)
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
                val coTeachersCount = coTeacherService.getCoTeacherCountByClassroomId(it.id!!)
                val blocked = calculateBlockedForSingleClassroom(it)
                Classroom.fromEntity(it, studentsCount, projectsCount, coTeachersCount, blocked)
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
                val coTeachersCount = coTeacherService.getCoTeacherCountByClassroomId(it.id!!)
                val blocked = calculateBlockedForSingleClassroom(it)
                Classroom.fromEntity(it, studentsCount, projectsCount, coTeachersCount, blocked)
            }
    }

    private fun mapToClassrooms(
        classroomEntities: Iterable<ClassroomEntity>,
        isCoTeacher: Boolean = false
    ): List<Classroom> {
        val classroomIds = classroomEntities.map { it.id!! }
        val teacherIds = classroomEntities.map { it.teacherId }.toSet()

        val studentCounts = studentClassroomLinkService.getStudentCounts(classroomIds)
        val projectCounts = projectClassroomShareService.getProjectCountsByClassrooms(classroomIds)
        val coTeacherCounts = coTeacherService.getCoTeacherCountsByClassroomIds(classroomIds)
        val firstClassroomIds = getFirstClassroomIdsByTeachers(teacherIds)
        val teacherBlockedStatus = getTeacherBlockedStatusMap(teacherIds)

        return classroomEntities
            .map {
                Classroom.fromEntity(
                    classroomEntity = it,
                    studentCounts.getOrDefault(it.id!!, 0),
                    projectCounts.getOrDefault(it.id!!, 0),
                    coTeacherCounts.getOrDefault(it.id!!, 0),
                    calculateBlocked(it, firstClassroomIds, teacherBlockedStatus, isCoTeacher)
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

    private fun calculateBlocked(
        classroomEntity: ClassroomEntity,
        firstClassroomIds: Set<UUID>,
        teacherSubscriptions: Map<UUID, Boolean>,
        isCoTeacher: Boolean
    ): Boolean {
        if (isCoTeacher) {
            return teacherSubscriptions[classroomEntity.teacherId] ?: false
        }

        if (firstClassroomIds.contains(classroomEntity.id)) {
            return false
        }

        return teacherSubscriptions[classroomEntity.teacherId] ?: false
    }

    private fun getFirstClassroomIdsByTeachers(teacherIds: Collection<UUID>): Set<UUID> {
        if (teacherIds.isEmpty()) return emptySet()

        val allClassrooms = classroomEntityRepository.findAllByTeacherIdIn(teacherIds)
        return allClassrooms.groupBy { it.teacherId }
            .mapValues { (_, classrooms) -> classrooms.minByOrNull { it.createdAt ?: Long.MAX_VALUE }?.id }
            .values
            .filterNotNull()
            .toSet()
    }

    private fun getTeacherBlockedStatusMap(teacherIds: Collection<UUID>): Map<UUID, Boolean> {
        if (teacherIds.isEmpty()) return emptyMap()

        val now = clock.instant().toEpochMilli()
        val subscriptions = teacherSubscriptionService.getSubscriptionDtos(teacherIds.toSet())

        return subscriptions.associate { subscription ->
            subscription.teacherId to !subscription.hasProSubscription(now)
        }
    }

    private fun calculateBlockedForSingleClassroom(classroomEntity: ClassroomEntity): Boolean {
        val firstClassroom = classroomEntityRepository.findAllByTeacherId(classroomEntity.teacherId)
            .minByOrNull { it.createdAt ?: Long.MAX_VALUE }

        if (firstClassroom?.id == classroomEntity.id) {
            return false
        }

        val subscription = teacherSubscriptionService.getSubscriptionDto(classroomEntity.teacherId) ?: return false
        val now = clock.instant().toEpochMilli()
        return !subscription.hasProSubscription(now)
    }
}
