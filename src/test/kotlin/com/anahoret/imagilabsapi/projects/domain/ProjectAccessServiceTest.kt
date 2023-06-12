package com.anahoret.imagilabsapi.projects.domain

import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import com.anahoret.imagilabsapi.projectclassroomshare.domain.ProjectClassroomShareService
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Project access service")
class ProjectAccessServiceTest {

    private val projectClassroomShareService = mockk<ProjectClassroomShareService>()
    private val classroomService = mockk<ClassroomService>()
    private val studentProfileService = mockk<StudentProfileService>()
    private val coTeacherService = mockk<CoTeacherService>()

    private val projectAccessService = ProjectAccessServiceImpl(
        projectClassroomShareService, classroomService, studentProfileService, coTeacherService
    )

    private val projectId = UUID.randomUUID()
    private val classroomId = UUID.randomUUID()

    @Test
    fun `can edit project as project owner test`() {
        val testTeacher = testTeacher()
        val project = mockk<Project> {
            every { id } returns projectId
            every { ownerId} returns testTeacher.id
            every { ownerUserType } returns testTeacher.userType
        }

        every { projectClassroomShareService.getShares(projectId) } returns emptyList()

        assertTrue(projectAccessService.canEdit(testTeacher, project, null))
    }

    @Test
    fun `can co-teacher edit project test`() {
        val testTeacher = testTeacher()
        val project = mockk<Project> {
            every { id } returns projectId
            every { ownerId} returns UUID.randomUUID()
            every { ownerUserType } returns testTeacher.userType
        }

        every { projectClassroomShareService.getShares(projectId) } returns emptyList()
        every { coTeacherService.isLinkedToClassroom(classroomId, testTeacher.id) } returns true

        assertTrue(projectAccessService.canEdit(testTeacher, project, classroomId))
    }

    @Test
    fun `can delete project as project owner test`() {
        val testTeacher = testTeacher()
        val project = mockk<Project> {
            every { id } returns projectId
            every { ownerId} returns testTeacher.id
            every { ownerUserType } returns testTeacher.userType
        }

        every { projectClassroomShareService.getShares(projectId) } returns emptyList()

        assertTrue(projectAccessService.canDelete(testTeacher, project, classroomId))
    }

    @Test
    fun `can co-teacher delete project test`() {
        val testTeacher = testTeacher()

        val project = mockk<Project> {
            every { id } returns projectId
            every { ownerId} returns UUID.randomUUID()
            every { ownerUserType } returns testTeacher.userType
        }

        every { projectClassroomShareService.getShares(projectId) } returns emptyList()
        every { coTeacherService.isLinkedToClassroom(classroomId, testTeacher.id) } returns true

        assertTrue(projectAccessService.canDelete(testTeacher, project, classroomId))
    }

    @Test
    fun `can run project as project owner test`() {
        val testTeacher = testTeacher()
        val project = mockk<Project> {
            every { id } returns projectId
            every { ownerId} returns testTeacher.id
            every { ownerUserType } returns testTeacher.userType
        }

        assertTrue(projectAccessService.canRun(testTeacher, project, classroomId))
    }

    @Test
    fun `can co-teacher run project test`() {
        val testTeacher = testTeacher()

        val project = mockk<Project> {
            every { id } returns projectId
            every { ownerId} returns UUID.randomUUID()
            every { ownerUserType } returns testTeacher.userType
        }

        every { coTeacherService.isLinkedToClassroom(classroomId, testTeacher.id) } returns true

        assertTrue(projectAccessService.canRun(testTeacher, project, classroomId))
    }

    @Test
    fun `can share project as project owner test`() {
        val testTeacher = testTeacher()
        val classroomIds = listOf(classroomId)
        val project = mockk<Project> {
            every { id } returns projectId
            every { ownerId} returns testTeacher.id
            every { ownerUserType } returns testTeacher.userType
        }

        assertTrue(projectAccessService.canShare(testTeacher, project, classroomIds))
    }

    @Test
    fun `can co-teacher share project test`() {
        val testTeacher = testTeacher()
        val classroomIds = listOf(classroomId)
        val project = mockk<Project> {
            every { id } returns projectId
            every { ownerId} returns UUID.randomUUID()
            every { ownerUserType } returns testTeacher.userType
        }

        every { coTeacherService.getClassroomIdListByTeacherId(testTeacher.id) } returns classroomIds
        every { classroomService.listIdsByTeacher(testTeacher.id) } returns emptySet()

        assertTrue(projectAccessService.canShare(testTeacher, project, classroomIds))
    }

    @Test
    fun `can co-teacher unshare project with classroomId test`() {
        val testTeacher = testTeacher()
        val project = mockk<Project> {
            every { id } returns projectId
            every { ownerId} returns UUID.randomUUID()
            every { ownerUserType } returns testTeacher.userType
        }

        every { coTeacherService.isLinkedToClassroom(classroomId, testTeacher.id) } returns true

        assertTrue(projectAccessService.canUnshare(testTeacher, project, classroomId))
    }

    @Test
    fun `cannot co-teacher unshare project with classroomId test`() {
        val testTeacher = testTeacher()
        val project = mockk<Project> {
            every { id } returns projectId
            every { ownerId} returns UUID.randomUUID()
            every { ownerUserType } returns testTeacher.userType
        }

        every { coTeacherService.isLinkedToClassroom(classroomId, testTeacher.id) } returns true
        every { studentProfileService.getStudentById(project.ownerId) } returns null

        assertFalse(projectAccessService.canUnshare(testTeacher, project, null))
    }

    @Test
    fun `can co-teacher unshare project with classroomIds test`() {
        val testTeacher = testTeacher()
        val classroomIds = listOf(classroomId)
        val project = mockk<Project> {
            every { id } returns projectId
            every { ownerId} returns UUID.randomUUID()
            every { ownerUserType } returns testTeacher.userType
        }

        every { coTeacherService.getClassroomIdListByTeacherId(testTeacher.id) } returns classroomIds
        every { classroomService.listIdsByTeacher(testTeacher.id) } returns emptySet()

        assertTrue(projectAccessService.canUnshare(testTeacher, project, classroomIds))
    }

    @Test
    fun `can teacher unshare project with classroomIds test`() {
        val testTeacher = testTeacher()
        val classroomIds = listOf(classroomId)
        val project = mockk<Project> {
            every { id } returns projectId
            every { ownerId} returns UUID.randomUUID()
            every { ownerUserType } returns testTeacher.userType
        }

        every { coTeacherService.getClassroomIdListByTeacherId(testTeacher.id) } returns emptyList()
        every { classroomService.listIdsByTeacher(testTeacher.id) } returns setOf(classroomId)

        assertTrue(projectAccessService.canUnshare(testTeacher, project, classroomIds))
    }

    @Test
    fun `cannot co-teacher unshare project with classroomIds test`() {
        val testTeacher = testTeacher()
        val classroomIds = listOf(classroomId)
        val project = mockk<Project> {
            every { id } returns projectId
            every { ownerId} returns UUID.randomUUID()
            every { ownerUserType } returns testTeacher.userType
        }

        every { coTeacherService.getClassroomIdListByTeacherId(testTeacher.id) } returns emptyList()
        every { classroomService.listIdsByTeacher(testTeacher.id) } returns emptySet()
        every { studentProfileService.getStudentById(project.ownerId) } returns null

        assertFalse(projectAccessService.canUnshare(testTeacher, project, classroomIds))
    }

    @Test
    fun `can teacher get project test`() {
        val testTeacher = testTeacher()
        val project = mockk<Project> {
            every { id } returns projectId
            every { ownerId} returns testTeacher.id
            every { ownerUserType } returns testTeacher.userType
        }

        assertTrue(projectAccessService.canGet(testTeacher, project, null))
    }

    @Test
    fun `can co-teacher get project test`() {
        val testTeacher = testTeacher()
        val project = mockk<Project> {
            every { id } returns projectId
            every { ownerId} returns UUID.randomUUID()
            every { ownerUserType } returns testTeacher.userType
        }

        every { classroomService.listIdsByTeacher(testTeacher.id) } returns emptySet()
        every { studentProfileService.getStudentById(project.ownerId) } returns null
        every { coTeacherService.isLinkedToClassroom(classroomId, testTeacher.id) } returns true

        assertTrue(projectAccessService.canGet(testTeacher, project, classroomId))
    }

    @Test
    fun `cannot co-teacher get project test`() {
        val testTeacher = testTeacher()
        val project = mockk<Project> {
            every { id } returns projectId
            every { ownerId} returns UUID.randomUUID()
            every { ownerUserType } returns testTeacher.userType
        }

        every { classroomService.listIdsByTeacher(testTeacher.id) } returns emptySet()
        every { studentProfileService.getStudentById(project.ownerId) } returns null
        every { coTeacherService.isLinkedToClassroom(classroomId, testTeacher.id) } returns false

        assertFalse(projectAccessService.canGet(testTeacher, project, classroomId))
    }

    @Test
    fun `can get list for owner as owner test`() {
        val testTeacher = testTeacher()
        val ownerId = testTeacher.id

        assertTrue(projectAccessService.canListForOwner(testTeacher, ownerId, null))
    }

    @Test
    fun `can get list for owner as co-teacher test`() {
        val testTeacher = testTeacher()
        val ownerId = UUID.randomUUID()

        every { studentProfileService.getStudentById(ownerId) } returns null
        every { coTeacherService.isLinkedToClassroom(classroomId, testTeacher.id) } returns true

        assertTrue(projectAccessService.canListForOwner(testTeacher, ownerId, classroomId))
    }

    @Test
    fun `cannot get list for owner as co-teacher test`() {
        val testTeacher = testTeacher()
        val ownerId = UUID.randomUUID()

        every { studentProfileService.getStudentById(ownerId) } returns null
        every { coTeacherService.isLinkedToClassroom(classroomId, testTeacher.id) } returns false

        assertFalse(projectAccessService.canListForOwner(testTeacher, ownerId, classroomId))
    }
}
