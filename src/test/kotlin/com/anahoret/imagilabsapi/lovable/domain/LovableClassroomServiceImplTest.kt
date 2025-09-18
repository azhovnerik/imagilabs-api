package com.anahoret.imagilabsapi.lovable.domain

import com.anahoret.imagilabsapi.lovable.storage.LovableClassroomEntity
import com.anahoret.imagilabsapi.lovable.storage.LovableClassroomEntityRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class LovableClassroomServiceImplTest {

    private lateinit var repo: LovableClassroomEntityRepository
    private lateinit var service: LovableClassroomService

    @BeforeEach
    fun setUp() {
        repo = mockk(relaxed = true)
        service = LovableClassroomServiceImpl(repo)
    }

    @Test
    fun `enableIntegrationForClassroom creates entity when missing`() {
        val classroomId = UUID.randomUUID()
        every { repo.findOneByClassroomId(classroomId) } returns null
        every { repo.save(any<LovableClassroomEntity>()) } answers { firstArg() }

        service.enableIntegrationForClassroom(classroomId)

        verifySequence {
            repo.findOneByClassroomId(classroomId)
            repo.save(match { it.classroomId == classroomId && it.lovableIntegrationEnabled })
        }
    }

    @Test
    fun `enableIntegrationForClassroom does nothing when already exists`() {
        val classroomId = UUID.randomUUID()
        val existing = LovableClassroomEntity(classroomId)
        every { repo.findOneByClassroomId(classroomId) } returns existing

        service.enableIntegrationForClassroom(classroomId)

        verify(exactly = 1) { repo.findOneByClassroomId(classroomId) }
        verify(inverse = true) { repo.save(any<LovableClassroomEntity>()) }
        confirmVerified(repo)
    }

    @Test
    fun `setPausedIntegrationForClassroom updates when exists`() {
        val classroomId = UUID.randomUUID()
        val existing = LovableClassroomEntity(classroomId)
        every { repo.findOneByClassroomId(classroomId) } returns existing
        every { repo.save(existing) } returns existing

        service.setPausedIntegrationForClassroom(classroomId, true)

        assertTrue(existing.lovableIntegrationPaused)
        verifySequence {
            repo.findOneByClassroomId(classroomId)
            repo.save(existing)
        }
    }

    @Test
    fun `setPausedIntegrationForClassroom does nothing when missing`() {
        val classroomId = UUID.randomUUID()
        every { repo.findOneByClassroomId(classroomId) } returns null

        service.setPausedIntegrationForClassroom(classroomId, true)

        verify(exactly = 1) { repo.findOneByClassroomId(classroomId) }
        verify(inverse = true) { repo.save(any<LovableClassroomEntity>()) }
    }

    @Test
    fun `integrationEnabledForClassroom returns true when enabled`() {
        val classroomId = UUID.randomUUID()
        val existing = LovableClassroomEntity(classroomId)
        every { repo.findOneByClassroomId(classroomId) } returns existing

        val result = service.integrationEnabledForClassroom(classroomId)

        assertTrue(result)
    }

    @Test
    fun `integrationEnabledForClassroom returns false when missing`() {
        val classroomId = UUID.randomUUID()
        every { repo.findOneByClassroomId(classroomId) } returns null

        val result = service.integrationEnabledForClassroom(classroomId)

        assertFalse(result)
    }

    @Test
    fun `integrationPausedForClassroom returns flag or false by default`() {
        val classroomId = UUID.randomUUID()
        val existing = LovableClassroomEntity(classroomId)
        existing.lovableIntegrationPaused = true
        every { repo.findOneByClassroomId(classroomId) } returns existing

        assertTrue(service.integrationPausedForClassroom(classroomId))

        every { repo.findOneByClassroomId(classroomId) } returns null
        assertFalse(service.integrationPausedForClassroom(classroomId))
    }
}
