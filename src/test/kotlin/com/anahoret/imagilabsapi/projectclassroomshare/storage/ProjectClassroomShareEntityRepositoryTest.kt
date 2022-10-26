package com.anahoret.imagilabsapi.projectclassroomshare.storage

import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntity
import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntityRepository
import com.anahoret.imagilabsapi.projects.storage.ProjectEntity
import com.anahoret.imagilabsapi.projects.storage.ProjectEntityRepository
import com.anahoret.imagilabsapi.spring.ImagiLabsDatabaseTest
import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import com.anahoret.imagilabsapi.students.storage.StudentProfileEntityRepository
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import com.anahoret.imagilabsapi.users.UserType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

@ImagiLabsDatabaseTest
@DisplayName("Project entity repository")
class ProjectClassroomShareEntityRepositoryTest {

    @Autowired
    lateinit var projectEntityRepository: ProjectEntityRepository

    @Autowired
    lateinit var teacherProfileEntityRepository: TeacherProfileEntityRepository

    @Autowired
    lateinit var studentProfileEntityRepository: StudentProfileEntityRepository

    @Autowired
    lateinit var classroomEntityRepository: ClassroomEntityRepository

    @Autowired
    lateinit var projectClassroomShareEntityRepository: ProjectClassroomShareEntityRepository

    @DisplayName("when search for projects")
    @Nested
    inner class SearchProjectsTest {

        private lateinit var classroom1Id: UUID
        private lateinit var classroom2Id: UUID
        private lateinit var classroom1StudentIds: List<UUID>
        private lateinit var classroom2StudentIds: List<UUID>
        private lateinit var eddardId: UUID
        private lateinit var sansaId: UUID
        private lateinit var brandonId: UUID
        private lateinit var theonId: UUID
        private lateinit var eddardProjectIds: List<UUID>
        private lateinit var sansaProjectIds: List<UUID>
        private lateinit var brandonProjectIds: List<UUID>
        private lateinit var theonProjectIds: List<UUID>
        private lateinit var classroom1ProjectIds: List<UUID>
        private lateinit var classroom2ProjectIds: List<UUID>

        @BeforeEach
        fun setup() {
            setupTeacher()
            setupClassrooms()
            setupStudents()
            setupProjects()
        }

        private fun setupTeacher() {
            eddardId = teacherProfileEntityRepository.save(
                TeacherProfileEntity("teacher@mail.com", "", "Eddard", "Stark", "", "", "", true)
            ).id!!
        }

        private fun setupClassrooms() {
            val classroom1 = classroomEntityRepository.save(ClassroomEntity("", "access1", eddardId))
            val classroom2 = classroomEntityRepository.save(ClassroomEntity("", "access2", eddardId))
            classroom1Id = classroom1.id!!
            classroom2Id = classroom2.id!!
        }

        private fun setupStudents() {
            val classroom1Students = listOf(
                StudentProfileEntity("Rob Stark", "rstark", "rstark", classroom1Id),
                StudentProfileEntity("Sansa Stark", "sstark", "sstark", classroom1Id),
                StudentProfileEntity("Aria Stark", "astark", "astark", classroom1Id),
                StudentProfileEntity("Brandon Stark", "bstark", "bstark", classroom1Id),
                StudentProfileEntity("Rikon Stark", "ristark", "ristark", classroom1Id),
            ).let(studentProfileEntityRepository::saveAll)
            sansaId = classroom1Students.find { it.username == "sstark" }?.id!!
            brandonId = classroom1Students.find { it.username == "bstark" }?.id!!
            classroom1StudentIds = classroom1Students.map { it.id!! }

            val classroom2Students = listOf(
                StudentProfileEntity("Theon Greyjoy", "tgrey", "tgrey", classroom2Id),
                StudentProfileEntity("John Snow", "jsnow", "jsnow", classroom2Id),
            ).let(studentProfileEntityRepository::saveAll)
            classroom2StudentIds = classroom2Students.map { it.id!! }
            theonId = classroom2Students.find { it.username == "tgrey" }?.id!!
        }

        private fun setupProjects() {
            eddardProjectIds = (1..2).map { ProjectEntity("Edd$it", eddardId, UserType.TEACHER, "") }
                .let(projectEntityRepository::saveAll).toList().map { it.id!! }
                .onEach { projectClassroomShareEntityRepository.save(ProjectClassroomShareEntity(it, classroom1Id)) }

            sansaProjectIds = (1..3).map { ProjectEntity("Sproj$it", sansaId, UserType.STUDENT, "") }
                .let(projectEntityRepository::saveAll).toList().map { it.id!! }
                .onEach { projectClassroomShareEntityRepository.save(ProjectClassroomShareEntity(it, classroom1Id)) }

            brandonProjectIds = (1..1).map { ProjectEntity("Brran", brandonId, UserType.STUDENT, "") }
                .let(projectEntityRepository::saveAll).toList().map { it.id!! }
                .onEach { projectClassroomShareEntityRepository.save(ProjectClassroomShareEntity(it, classroom1Id)) }

            theonProjectIds = (1..5).map { ProjectEntity("Thproj$it", theonId, UserType.STUDENT, "") }
                .let(projectEntityRepository::saveAll).toList().map { it.id!! }
                .onEach { projectClassroomShareEntityRepository.save(ProjectClassroomShareEntity(it, classroom2Id)) }

            classroom1ProjectIds = eddardProjectIds + sansaProjectIds + brandonProjectIds
            classroom2ProjectIds = theonProjectIds
        }

        @Test
        fun `should find projects by classroom ID if filters are not set`() {
            val result = projectClassroomShareEntityRepository.search(classroom1Id, null, null)
                .map { it.projectId }.toSet()
            assertEquals(classroom1ProjectIds.toSet(), result)
        }

        @Test
        fun `should find projects by classroom ID and owner ID`() {
            val result = projectClassroomShareEntityRepository.search(classroom1Id, eddardId, null)
                .map { it.projectId }.toSet()
            assertEquals(eddardProjectIds.toSet(), result)
        }

        @Test
        fun `should find projects by classroom ID and teacher name`() {
            val result = projectClassroomShareEntityRepository.search(classroom1Id, null, "Eddard")
                .map { it.projectId }.toSet()
            assertEquals(eddardProjectIds.toSet(), result)
        }

        @Test
        fun `should find projects by classroom ID and student name`() {
            val result = projectClassroomShareEntityRepository.search(classroom1Id, null, "sansa")
                .map { it.projectId }.toSet()
            assertEquals(sansaProjectIds.toSet(), result)
        }

        @Test
        fun `should find projects by classroom ID and project name`() {
            val result = projectClassroomShareEntityRepository.search(classroom1Id, null, "sproj")
                .map { it.projectId }.toSet()
            assertEquals(sansaProjectIds.toSet(), result)
        }

    }

}
