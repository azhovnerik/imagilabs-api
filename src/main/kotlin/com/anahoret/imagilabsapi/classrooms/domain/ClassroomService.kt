package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntity
import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntityRepository
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.stereotype.Service
import java.util.*

interface ClassroomService {

    fun create(teacherId: UUID, classroomCreateRequest: ClassroomCreateRequest): Classroom
    fun countByTeacher(teacherId: UUID): Long
    fun listByTeacher(teacherId: UUID): List<Classroom>

}

@Service
class ClassroomServiceImpl(
    private val classroomEntityRepository: ClassroomEntityRepository
) : ClassroomService {

    override fun create(teacherId: UUID, classroomCreateRequest: ClassroomCreateRequest): Classroom {
        val accessCode = generateUniqueAccessCode()
        return classroomEntityRepository.save(
            ClassroomEntity(
                classroomCreateRequest.name,
                accessCode,
                teacherId
            )
        ).let(Classroom.Companion::fromEntity)
    }

    override fun countByTeacher(teacherId: UUID): Long {
        return classroomEntityRepository.countByTeacherId(teacherId)
    }

    override fun listByTeacher(teacherId: UUID): List<Classroom> {
        return classroomEntityRepository.findAllByTeacherId(teacherId)
            .map(Classroom.Companion::fromEntity)
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
