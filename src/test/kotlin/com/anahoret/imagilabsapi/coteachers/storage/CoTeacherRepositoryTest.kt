package com.anahoret.imagilabsapi.coteachers.storage

import com.anahoret.imagilabsapi.classrooms.domain.TeacherRole.CO_TEACHER
import com.anahoret.imagilabsapi.spring.ImagiLabsDatabaseTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

@ImagiLabsDatabaseTest
@DisplayName("Co teacher entity repository")
class CoTeacherRepositoryTest {

    @Autowired
    lateinit var coTeacherRepository: CoTeacherRepository

    lateinit var coTeacherIds: List<UUID>
    lateinit var teacherId: UUID

    @BeforeEach
    fun setup() {
        teacherId = UUID.randomUUID()
        coTeacherIds = coTeacherRepository.saveAll(listOf(
            CoTeacherEntity(UUID.randomUUID(), "teacher1@gmail.com", teacherId),
            CoTeacherEntity(UUID.randomUUID(), "teacher2@gmail.com", teacherId, CO_TEACHER),
            CoTeacherEntity(UUID.randomUUID(), "teacher3@gmail.com", teacherId, CO_TEACHER),
            CoTeacherEntity(UUID.randomUUID(), "teacher4@gmail.com", teacherId)
        )).map { it.id!! }
    }

    @DisplayName("getting classroom ids by teacher id")
    @Nested
    inner class GetClassroomIdsByTeacherId {

        @Test
        fun `should return list of classroom id`() {
            val result = coTeacherRepository.getClassroomIdsByTeacherId(teacherId)
            assertFalse(result.isEmpty())
        }
    }
}
