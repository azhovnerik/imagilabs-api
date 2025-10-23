package com.anahoret.imagilabsapi.students.domain

import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntityRepository
import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import com.anahoret.imagilabsapi.students.storage.StudentProfileEntityRepository
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Clock
import java.util.*

interface StudentProfileService {

    fun createStudents(
        classroomId: UUID,
        studentCreateRequests: List<StudentCreateRequest>
    ): List<StudentProfile>

    fun listStudentCredentialsCardsByIds(
        studentIds: Collection<UUID>,
        classroom: Classroom,
        projectCounts: Map<UUID, Long>,
        sharedProjectCounts: Map<UUID, Long>
    ): List<StudentClassroomCard>

    fun getStudentById(studentId: UUID): StudentProfile?
    fun getStudentDetailsById(studentId: UUID): StudentDetails?
    fun listByIds(ids: List<UUID>): List<StudentProfile>
    fun getStudentCredentials(studentLoginRequest: StudentLoginRequest): StudentCredentials?
    fun getStudentCredentials(studentId: UUID): StudentCredentials?
    fun delete(studentId: UUID)
    fun delete(studentIds: Collection<UUID>)
    fun update(studentId: UUID, studentUpdateRequest: StudentUpdateRequest): StudentProfile?
    fun studentCredentialsExists(studentClassroomCredentials: StudentClassroomCredentials): Boolean
    fun listByClassroom(classroomId: UUID): List<StudentProfile>
    fun listByClassroom(classroomId: UUID, searchQuery: String?, sort: Sort): List<StudentProfile>
    fun resetPassword(studentId: UUID): StudentCredentials?
    fun completeChatOnboarding(studentId: UUID): StudentProfile?
    fun isAiChatOnboardingCompleted(studentId: UUID): Boolean
    fun getByEdLink(edLinkIntegrationId: UUID, edLinkPersonId: UUID): StudentProfile?
}

@Service
class StudentProfileServiceImpl(
    private val studentProfileEntityRepository: StudentProfileEntityRepository,
    private val classroomEntityRepository: ClassroomEntityRepository,
    private val clock: Clock,
    @Value($$"${spring.ai.openai.tip-tokens-per-hour}") private val tipTokens: Int
) : StudentProfileService {

    companion object {

        const val USERNAME_LENGTH_LIMIT = 15
    }

    override fun createStudents(
        classroomId: UUID,
        studentCreateRequests: List<StudentCreateRequest>
    ): List<StudentProfile> {
        val existingUserNames = studentProfileEntityRepository
            .findAllByClassroomId(classroomId)
            .map(StudentProfileEntity::username)
            .toMutableSet()
        return studentCreateRequests.map {
            val username = createUniqueStudentUsername(it.name, existingUserNames)
            existingUserNames.add(username)
            val password = createStudentPassword()
            StudentProfileEntity(
                it.name,
                username,
                password,
                classroomId,
                tipTokens,
                tipTokensReplenishedAt = clock.millis(),
                edLinkIntegrationId = it.edLinkIntegrationId,
                edLinkPersonId = it.edLinkPersonId
            )
        }.let(studentProfileEntityRepository::saveAll)
            .map(StudentProfile.Companion::fromEntity)
    }

    override fun listStudentCredentialsCardsByIds(
        studentIds: Collection<UUID>,
        classroom: Classroom,
        projectCounts: Map<UUID, Long>,
        sharedProjectCounts: Map<UUID, Long>
    ): List<StudentClassroomCard> {
        return studentProfileEntityRepository.findAllById(studentIds)
            .map {
                val sharedProjectsCount = sharedProjectCounts.getOrDefault(it.id, 0)
                val draftProjectsCount = projectCounts.getOrDefault(it.id, 0) - sharedProjectsCount
                StudentClassroomCard.fromEntity(
                    studentProfileEntity = it,
                    classroom = classroom,
                    sharedProjectsCount = sharedProjectsCount,
                    draftProjectsCount = draftProjectsCount
                )
            }
    }

    override fun getStudentById(studentId: UUID): StudentProfile? {
        return studentProfileEntityRepository.findByIdOrNull(studentId)
            ?.let(StudentProfile.Companion::fromEntity)
    }

    override fun getStudentDetailsById(studentId: UUID): StudentDetails? {
        val studentProfileEntity = studentProfileEntityRepository.findByIdOrNull(studentId) ?: return null
        val classroomEntity = classroomEntityRepository.findByIdOrNull(studentProfileEntity.classroomId) ?: return null
        return StudentDetails.fromEntity(studentProfileEntity, classroomEntity)
    }

    override fun listByIds(ids: List<UUID>): List<StudentProfile> {
        return studentProfileEntityRepository.findAllById(ids)
            .map(StudentProfile.Companion::fromEntity)
    }

    override fun getStudentCredentials(studentLoginRequest: StudentLoginRequest): StudentCredentials? {
        return with(studentLoginRequest) {
            studentProfileEntityRepository.findByCredentials(username, password, classroomAccessCode)
                ?.let(StudentCredentials.Companion::fromEntity)
        }
    }

    override fun getStudentCredentials(studentId: UUID): StudentCredentials? {
        return studentProfileEntityRepository.findByIdOrNull(studentId)
            ?.let(StudentCredentials.Companion::fromEntity)
    }

    override fun studentCredentialsExists(studentClassroomCredentials: StudentClassroomCredentials): Boolean {
        return with(studentClassroomCredentials) {
            studentProfileEntityRepository.findByCredentials(username, password, classroomAccessCode)
        } != null
    }

    override fun delete(studentId: UUID) {
        studentProfileEntityRepository.deleteById(studentId)
    }

    override fun delete(studentIds: Collection<UUID>) {
        if (studentIds.isEmpty()) return
        studentProfileEntityRepository.deleteByIdIn(studentIds)
    }

    override fun update(studentId: UUID, studentUpdateRequest: StudentUpdateRequest): StudentProfile? {
        return studentProfileEntityRepository.findByIdOrNull(studentId)?.let {
            it.name = studentUpdateRequest.name
            it.username = studentUpdateRequest.username
            studentProfileEntityRepository.save(it)
        }?.let(StudentProfile.Companion::fromEntity)
    }

    override fun listByClassroom(classroomId: UUID): List<StudentProfile> {
        return doListByClassroom(classroomId, Sort.unsorted())
    }

    override fun listByClassroom(classroomId: UUID, searchQuery: String?, sort: Sort): List<StudentProfile> {
        return if (searchQuery == null) doListByClassroom(classroomId, sort)
        else studentProfileEntityRepository.findAllByClassroomId(classroomId, searchQuery, sort)
            .map(StudentProfile.Companion::fromEntity)
    }

    override fun resetPassword(studentId: UUID): StudentCredentials? {
        return studentProfileEntityRepository.findByIdOrNull(studentId)?.let {
            it.password = createStudentPassword()
            studentProfileEntityRepository.save(it)
        }?.let(StudentCredentials.Companion::fromEntity)
    }

    override fun completeChatOnboarding(studentId: UUID): StudentProfile? {
        return studentProfileEntityRepository.findByIdOrNull(studentId)?.let {
            it.aiChatOnboardingCompleted = true
            studentProfileEntityRepository.save(it)
        }?.let(StudentProfile.Companion::fromEntity)
    }

    override fun getByEdLink(edLinkIntegrationId: UUID, edLinkPersonId: UUID): StudentProfile? {
        return studentProfileEntityRepository
            .findOneByEdLinkIntegrationIdAndEdLinkPersonId(edLinkIntegrationId, edLinkPersonId)
            ?.let(StudentProfile.Companion::fromEntity)
    }

    private fun doListByClassroom(classroomId: UUID, sort: Sort): List<StudentProfile> {
        return studentProfileEntityRepository.findAllByClassroomId(classroomId, sort)
            .map(StudentProfile.Companion::fromEntity)
    }

    private fun createStudentPassword(): String {
        return RandomStringUtils.randomAlphabetic(8).uppercase()
    }

    private fun createUniqueStudentUsername(name: String, existingUserNames: Set<String>): String {
        val split = name.split("\\s+".toRegex())
        val prefix = when (split.size) {
            1 -> name
            else -> split[0].trim() + split[1].trim().first()
        }.replace(Regex("['`]"), "")

        for (i in 0..199) {
            val suffix = i.takeIf { it > 0 }?.toString().orEmpty()
            val username = prefix.take(USERNAME_LENGTH_LIMIT - suffix.length) + suffix
            if (username !in existingUserNames) return username
        }

        throw RuntimeException("EXCEEDED_NUMBER_OF_ATTEMPTS_TO_GENERATE_UNIQUE_USERNAME_FOR_STUDENT")
    }

    override fun isAiChatOnboardingCompleted(studentId: UUID): Boolean {
        return studentProfileEntityRepository.isAiChatOnboardingCompleted(studentId)
    }
}
