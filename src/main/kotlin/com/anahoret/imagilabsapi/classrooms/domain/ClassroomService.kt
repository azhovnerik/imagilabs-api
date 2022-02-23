package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntity
import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntityRepository
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.stereotype.Service
import java.util.*

interface ClassroomService {

    fun create(teacherId: UUID, classroomCreateRequest: ClassroomCreateRequest): Classroom
    fun countByTeacher(teacherId: UUID): Long

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

    private fun generateUniqueAccessCode(): String {
        val accessCode = RandomStringUtils.randomAlphanumeric(6)
        for (i in 1..100) {
            if (classroomEntityRepository.findByAccessCode(accessCode) == null) {
                return accessCode
            }
        }
        throw RuntimeException("Exceeded number of attempts to generate unique code for class")
    }
}
