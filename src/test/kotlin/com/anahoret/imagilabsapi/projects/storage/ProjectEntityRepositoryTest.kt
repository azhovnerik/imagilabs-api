package com.anahoret.imagilabsapi.projects.storage

import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntity
import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntityRepository
import com.anahoret.imagilabsapi.projectclassroomshare.storage.ProjectClassroomShareEntity
import com.anahoret.imagilabsapi.projectclassroomshare.storage.ProjectClassroomShareEntityRepository
import com.anahoret.imagilabsapi.projects.domain.ProjectState
import com.anahoret.imagilabsapi.spring.ImagiLabsDatabaseTest
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import com.anahoret.imagilabsapi.users.UserType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import java.util.*

@ImagiLabsDatabaseTest
@DisplayName("Project entity repository")
class ProjectEntityRepositoryTest {

    @Autowired
    lateinit var projectEntityRepository: ProjectEntityRepository

    @Autowired
    lateinit var teacherProfileEntityRepository: TeacherProfileEntityRepository

    @Autowired
    lateinit var classroomEntityRepository: ClassroomEntityRepository

    @Autowired
    lateinit var projectClassroomShareEntityRepository: ProjectClassroomShareEntityRepository

    @DisplayName("when search for projects")
    @Nested
    inner class SearchProjectsTest {

        private lateinit var ownerId: UUID
        private lateinit var ownerProjectIds: List<UUID>
        private lateinit var ownerSharedProjectIds: List<UUID>
        private lateinit var ownerSharedProjectIds2: List<UUID>
        private lateinit var ownerDraftProjects: List<UUID>
        private lateinit var ownerClassroomId: UUID
        private lateinit var ownerClassroomId2: UUID

        private lateinit var nonOwnerId: UUID
        private lateinit var nonOwnerProjectIds: List<UUID>
        private lateinit var nonOwnerClassroomId: UUID
        private lateinit var nonOwnerSharedProjectIds: List<UUID>

        @BeforeEach
        fun setup() {
            setupOwnerProjects()
            setupOwnerClassroom()
            shareOwnerProjects()

            setupNonOwnerProjects()
            setupNonOwnerClassroom()
            shareNonOwnerProjects()
        }

        private fun shareOwnerProjects() {
            ownerSharedProjectIds = listOf(ownerProjectIds[0], ownerProjectIds[2])
            ownerSharedProjectIds.forEach {
                projectClassroomShareEntityRepository.save(
                    ProjectClassroomShareEntity(it, ownerClassroomId)
                )
            }

            ownerSharedProjectIds2 = listOf(ownerProjectIds[1], ownerProjectIds[2])
            ownerSharedProjectIds2.forEach {
                projectClassroomShareEntityRepository.save(
                    ProjectClassroomShareEntity(it, ownerClassroomId2)
                )
            }

            ownerDraftProjects = ownerProjectIds - ownerSharedProjectIds.toSet() - ownerSharedProjectIds2.toSet()
        }

        private fun setupOwnerClassroom() {
            val classroom = classroomEntityRepository.save(ClassroomEntity("classroom", "owneraccess", ownerId))
            ownerClassroomId = classroom.id!!

            val classroom2 = classroomEntityRepository.save(ClassroomEntity("classroom", "owneraccess2", ownerId))
            ownerClassroomId2 = classroom2.id!!
        }

        private fun setupOwnerProjects() {
            val owner = teacherProfileEntityRepository.save(
                TeacherProfileEntity("owner", "", "", "", "", "", "", true)
            )
            ownerId = owner.id!!
            val ownerProjects = projectEntityRepository.saveAll((1..5).map { index ->
                ProjectEntity("Project $index", ownerId, UserType.TEACHER, "", null)
            })
            ownerProjectIds = ownerProjects.map { it.id!! }
        }

        private fun setupNonOwnerProjects() {
            val nonOwner = teacherProfileEntityRepository.save(
                TeacherProfileEntity("nonowner", "", "", "", "", "", "", true)
            )
            nonOwnerId = nonOwner.id!!
            val nonOwnerProjects = projectEntityRepository.saveAll((1..3).map { index ->
                ProjectEntity("Project $index", nonOwnerId, UserType.TEACHER, "", null)
            })
            nonOwnerProjectIds = nonOwnerProjects.map { it.id!! }
        }

        private fun setupNonOwnerClassroom() {
            val classroom = classroomEntityRepository.save(ClassroomEntity("classroom", "nonowneraccess", nonOwnerId))
            nonOwnerClassroomId = classroom.id!!
        }

        private fun shareNonOwnerProjects() {
            nonOwnerSharedProjectIds = listOf(nonOwnerProjectIds[0], nonOwnerProjectIds[2])
            nonOwnerSharedProjectIds.forEach {
                projectClassroomShareEntityRepository.save(
                    ProjectClassroomShareEntity(it, nonOwnerClassroomId)
                )
            }
        }

        @Test
        fun `should return projects by owner ID if filters are not set`() {
            val searchResult = projectEntityRepository.search(ownerId, null, null, Pageable.unpaged())
            assertEquals(ownerProjectIds.toSet(), searchResult.map { it.id }.toSet())
        }

        @Test
        fun `should return shared projects`() {
            val searchResult = projectEntityRepository.search(ownerId, ProjectState.SHARED, null, Pageable.unpaged())
            assertEquals(
                (ownerSharedProjectIds + ownerSharedProjectIds2).toSet(),
                searchResult.map { it.id }.toSet()
            )
        }

        @Test
        fun `should return draft projects`() {
            val searchResult = projectEntityRepository.search(ownerId, ProjectState.DRAFT, null, Pageable.unpaged())
            assertEquals(ownerDraftProjects.toSet(), searchResult.map { it.id }.toSet())
        }

        @Test
        fun `should return projects shared in single classroom`() {
            val searchResult =
                projectEntityRepository.search(ownerId, null, setOf(ownerClassroomId), Pageable.unpaged())
            assertEquals(ownerSharedProjectIds.toSet(), searchResult.map { it.id }.toSet())
        }

        @Test
        fun `should return projects shared in multiple classrooms`() {
            val searchResult = projectEntityRepository.search(
                ownerId,
                null,
                setOf(ownerClassroomId, ownerClassroomId2),
                Pageable.unpaged()
            )
            assertEquals(
                (ownerSharedProjectIds + ownerSharedProjectIds2).toSet(),
                searchResult.map { it.id }.toSet()
            )
        }

        @Test
        fun `should return empty list if shared in classroom is not empty and status is draft`() {
            val searchResult = projectEntityRepository.search(
                ownerId,
                ProjectState.DRAFT,
                setOf(ownerClassroomId, ownerClassroomId2),
                Pageable.unpaged()
            )
            assertTrue(searchResult.isEmpty())
        }

        @Test
        fun `should delete projects by ownerId`() {
            projectClassroomShareEntityRepository.deleteAllByProjectIdIn(ownerProjectIds)
            projectEntityRepository.deleteAllByOwnerId(ownerId)
            val searchResult = projectEntityRepository.search(ownerId, null, null, Pageable.unpaged())
            assertNotEquals(ownerProjectIds.toSet(), searchResult.map { it.id }.toSet())
        }

        @Test
        fun `should delete projects by projectIds`() {
            projectClassroomShareEntityRepository.deleteAllByProjectIdIn(nonOwnerProjectIds)
            projectEntityRepository.deleteAllById(nonOwnerProjectIds)
            val searchResult = projectEntityRepository.search(nonOwnerId, null, null, Pageable.unpaged())
            assertNotEquals(nonOwnerProjectIds.toSet(), searchResult.map { it.id }.toSet())
        }
    }
}
