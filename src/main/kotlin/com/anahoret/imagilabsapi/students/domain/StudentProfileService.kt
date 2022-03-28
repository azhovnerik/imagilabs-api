package com.anahoret.imagilabsapi.students.domain

import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import com.anahoret.imagilabsapi.students.storage.StudentProfileEntityRepository
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

interface StudentProfileService {

    fun createStudents(
        classroomId: UUID,
        studentCreateRequests: List<StudentCreateRequest>
    ): List<StudentProfile>

    fun listStudentCredentialsCardsByIds(
        studentIds: Iterable<UUID>,
        classroomAccessCode: String,
        projectCounts: Map<UUID, Long>,
        sharedProjectCounts: Map<UUID, Long>
    ): List<StudentClassroomCard>

    fun getStudentById(studentId: UUID): StudentProfile?
    fun listByIds(ids: List<UUID>): List<StudentProfile>
    fun getStudentCredentials(studentLoginRequest: StudentLoginRequest): StudentCredentials?
    fun delete(studentId: UUID)
    fun delete(studentIds: Collection<UUID>)

}

@Service
class StudentProfileServiceImpl(
    private val studentProfileEntityRepository: StudentProfileEntityRepository
) : StudentProfileService {

    companion object {

        const val USERNAME_LENGTH_LIMIT = 15
    }

    override fun createStudents(
        classroomId: UUID,
        studentCreateRequests: List<StudentCreateRequest>
    ): List<StudentProfile> {
        val existingUserNames = studentProfileEntityRepository
            .findAllByClassroom(classroomId)
            .map(StudentProfileEntity::username)
            .toMutableSet()
        return studentCreateRequests.map {
            val username = createUniqueStudentUsername(it.name, existingUserNames)
            existingUserNames.add(username)
            val password = createStudentPassword()
            StudentProfileEntity(it.name, username, password)
        }.let(studentProfileEntityRepository::saveAll)
            .map(StudentProfile.Companion::fromEntity)
    }

    override fun listStudentCredentialsCardsByIds(
        studentIds: Iterable<UUID>,
        classroomAccessCode: String,
        projectCounts: Map<UUID, Long>,
        sharedProjectCounts: Map<UUID, Long>
    ): List<StudentClassroomCard> {
        return studentProfileEntityRepository.findAllById(studentIds)
            .map {
                val sharedProjectsCount = sharedProjectCounts.getOrDefault(it.id, 0)
                val draftProjectsCount = projectCounts.getOrDefault(it.id, 0) - sharedProjectsCount
                StudentClassroomCard.fromEntity(
                    it,
                    classroomAccessCode,
                    sharedProjectsCount,
                    draftProjectsCount
                )
            }
    }

    override fun getStudentById(studentId: UUID): StudentProfile? {
        return studentProfileEntityRepository.findByIdOrNull(studentId)
            ?.let(StudentProfile.Companion::fromEntity)
    }

    override fun listByIds(ids: List<UUID>): List<StudentProfile> {
        return studentProfileEntityRepository.findAllById(ids)
            .map(StudentProfile.Companion::fromEntity)
    }

    override fun getStudentCredentials(studentLoginRequest: StudentLoginRequest): StudentCredentials? {
        return with(studentLoginRequest) {
            studentProfileEntityRepository.findByCredentials(username, password, classroomAccessCode)
                ?.let { StudentCredentials.fromEntity(it, classroomAccessCode) }
        }
    }

    override fun delete(studentId: UUID) {
        studentProfileEntityRepository.deleteById(studentId)
    }

    override fun delete(studentIds: Collection<UUID>) {
        if (studentIds.isEmpty()) return
        studentProfileEntityRepository.deleteByIdIn(studentIds)
    }

    private fun createStudentPassword(): String {
        return RandomStringUtils.randomAlphanumeric(8)
    }

    private fun createUniqueStudentUsername(name: String, existingUserNames: Set<String>): String {
        val split = name.split("\\s+".toRegex())
        val prefix = when (split.size) {
            1 -> name
            else -> split[0].trim() + split[1].trim().first()
        }

        for (i in 0..199) {
            val suffix = i.takeIf { it > 0 }?.toString().orEmpty()
            val username = prefix.take(USERNAME_LENGTH_LIMIT - suffix.length) + suffix
            if (username !in existingUserNames) return username
        }

        throw RuntimeException("EXCEEDED_NUMBER_OF_ATTEMPTS_TO_GENERATE_UNIQUE_USERNAME_FOR_STUDENT")
    }
}
