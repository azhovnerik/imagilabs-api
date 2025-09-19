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
    fun `enableIntegrationForClassroom creates entity when absent`() {
        val classroomId = UUID.randomUUID()
        every { repo.findOneByClassroomId(classroomId) } returns null
        every { repo.save(any<LovableClassroomEntity>()) } answers { firstArg() }

        service.enableIntegrationForClassroom(classroomId)

        verifySequence {
            repo.findOneByClassroomId(classroomId)
            repo.save(match { it.classroomId == classroomId && it.lovableIntegrationEnabled && !it.lovableIntegrationPaused })
        }
        confirmVerified(repo)
    }

    @Test
    fun `enableIntegrationForClassroom does nothing when already exists`() {
        val classroomId = UUID.randomUUID()
        every { repo.findOneByClassroomId(classroomId) } returns LovableClassroomEntity(classroomId)

        service.enableIntegrationForClassroom(classroomId)

        verify { repo.findOneByClassroomId(classroomId) }
        verify(exactly = 0) { repo.save(any<LovableClassroomEntity>()) }
        confirmVerified(repo)
    }

    @Test
    fun `setPausedIntegrationForClassroom updates when exists`() {
        val classroomId = UUID.randomUUID()
        val entity =
            LovableClassroomEntity(classroomId, lovableIntegrationEnabled = true, lovableIntegrationPaused = false)
        every { repo.findOneByClassroomId(classroomId) } returns entity
        every { repo.save(any<LovableClassroomEntity>()) } answers { firstArg() }

        service.setPausedIntegrationForClassroom(classroomId, true)

        assertTrue(entity.lovableIntegrationPaused)
        verifySequence {
            repo.findOneByClassroomId(classroomId)
            repo.save(entity)
        }
        confirmVerified(repo)
    }

    @Test
    fun `setPausedIntegrationForClassroom does nothing when not exists`() {
        val classroomId = UUID.randomUUID()
        every { repo.findOneByClassroomId(classroomId) } returns null

        service.setPausedIntegrationForClassroom(classroomId, true)

        verify { repo.findOneByClassroomId(classroomId) }
        verify(exactly = 0) { repo.save(any<LovableClassroomEntity>()) }
        confirmVerified(repo)
    }

    @Test
    fun `integrationEnabledForClassroom returns flag when exists`() {
        val classroomId = UUID.randomUUID()
        every { repo.findOneByClassroomId(classroomId) } returns LovableClassroomEntity(
            classroomId,
            lovableIntegrationEnabled = true
        )

        val result = service.integrationEnabledForClassroom(classroomId)

        assertTrue(result)
        verify { repo.findOneByClassroomId(classroomId) }
    }

    @Test
    fun `integrationEnabledForClassroom returns false when not exists`() {
        val classroomId = UUID.randomUUID()
        every { repo.findOneByClassroomId(classroomId) } returns null

        val result = service.integrationEnabledForClassroom(classroomId)

        assertFalse(result)
        verify { repo.findOneByClassroomId(classroomId) }
    }

    @Test
    fun `integrationPausedForClassroom returns flag when exists`() {
        val classroomId = UUID.randomUUID()
        every { repo.findOneByClassroomId(classroomId) } returns LovableClassroomEntity(
            classroomId,
            lovableIntegrationPaused = true
        )

        val result = service.integrationPausedForClassroom(classroomId)

        assertTrue(result)
        verify { repo.findOneByClassroomId(classroomId) }
    }

    @Test
    fun `integrationPausedForClassroom returns false when not exists`() {
        val classroomId = UUID.randomUUID()
        every { repo.findOneByClassroomId(classroomId) } returns null

        val result = service.integrationPausedForClassroom(classroomId)

        assertFalse(result)
        verify { repo.findOneByClassroomId(classroomId) }
    }

    @Test
    fun `deleteForClassroom delegates to repository`() {
        val classroomId = UUID.randomUUID()

        service.deleteForClassroom(classroomId)

        verify { repo.deleteByClassroomId(classroomId) }
        confirmVerified(repo)
    }

    @Test
    fun `getIntegrationForClassroom returns mapped domain when entity exists`() {
        val classroomId = UUID.randomUUID()
        val entity = LovableClassroomEntity(
            classroomId = classroomId,
            lovableIntegrationEnabled = true,
            lovableIntegrationPaused = false
        )
        every { repo.getByClassroomId(classroomId) } returns entity

        val result = service.getIntegrationForClassroom(classroomId)

        assertTrue(result != null)
        assertTrue(result!!.classroomId == classroomId)
        assertTrue(result.lovableIntegrationEnabled)
        assertFalse(result.lovableIntegrationPaused)
        verify(exactly = 1) { repo.getByClassroomId(classroomId) }
        confirmVerified(repo)
    }

    @Test
    fun `getIntegrationForClassroom returns null when entity not found`() {
        val classroomId = UUID.randomUUID()
        every { repo.getByClassroomId(classroomId) } returns null

        val result = service.getIntegrationForClassroom(classroomId)

        assertTrue(result == null)
        verify(exactly = 1) { repo.getByClassroomId(classroomId) }
        confirmVerified(repo)
    }
}
