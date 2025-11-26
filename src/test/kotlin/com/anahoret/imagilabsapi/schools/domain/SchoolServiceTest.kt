package com.anahoret.imagilabsapi.schools.domain

import com.anahoret.imagilabsapi.schools.storage.SchoolEntity
import com.anahoret.imagilabsapi.schools.storage.SchoolRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import java.util.*

@DisplayName("School service")
class SchoolServiceTest {

    private val schoolRepository = mockk<SchoolRepository>()
    private val schoolService = SchoolServiceImpl(schoolRepository)

    @Test
    fun `should create school successfully`() {
        val schoolName = "Test School"
        val schoolEntity = SchoolEntity(schoolName).apply { id = UUID.randomUUID() }

        every { schoolRepository.existsByNameIgnoreCase(schoolName) } returns false
        every { schoolRepository.save(any()) } returns schoolEntity

        val result = schoolService.create(schoolName)

        assertEquals(schoolName, result.name)
        verify { schoolRepository.save(any()) }
    }

    @Test
    fun `should throw exception when creating school with existing name`() {
        val schoolName = "Test School"

        every { schoolRepository.existsByNameIgnoreCase(schoolName) } returns true

        assertThrows<IllegalArgumentException> {
            schoolService.create(schoolName)
        }
    }

    @Test
    fun `should get school by id`() {
        val schoolId = UUID.randomUUID()
        val schoolEntity = SchoolEntity("Test School").apply { id = schoolId }

        every { schoolRepository.findByIdOrNull(schoolId) } returns schoolEntity

        val result = schoolService.getById(schoolId)

        assertNotNull(result)
        assertEquals(schoolId, result?.id)
        assertEquals("Test School", result?.name)
    }

    @Test
    fun `should return null when school not found by id`() {
        val schoolId = UUID.randomUUID()

        every { schoolRepository.findByIdOrNull(schoolId) } returns null

        val result = schoolService.getById(schoolId)

        assertNull(result)
    }

    @Test
    fun `should list all schools sorted by name`() {
        val school1 = SchoolEntity("A School").apply { id = UUID.randomUUID() }
        val school2 = SchoolEntity("B School").apply { id = UUID.randomUUID() }

        every { schoolRepository.findAll(any<Sort>()) } returns listOf(school1, school2)

        val result = schoolService.listAll()

        assertEquals(2, result.size)
        assertEquals("A School", result[0].name)
        assertEquals("B School", result[1].name)
    }

    @Test
    fun `should update school successfully`() {
        val schoolId = UUID.randomUUID()
        val newName = "Updated School"
        val schoolEntity = SchoolEntity("Old School").apply { id = schoolId }

        every { schoolRepository.findByIdOrNull(schoolId) } returns schoolEntity
        every { schoolRepository.findByNameIgnoreCase(newName) } returns null
        every { schoolRepository.save(any()) } returns schoolEntity.apply { name = newName }

        val result = schoolService.update(schoolId, newName)

        assertNotNull(result)
        assertEquals(newName, result?.name)
        verify { schoolRepository.save(any()) }
    }

    @Test
    fun `should throw exception when updating to existing school name`() {
        val schoolId = UUID.randomUUID()
        val existingSchoolId = UUID.randomUUID()
        val newName = "Existing School"
        val schoolEntity = SchoolEntity("Old School").apply { id = schoolId }
        val existingSchool = SchoolEntity(newName).apply { id = existingSchoolId }

        every { schoolRepository.findByIdOrNull(schoolId) } returns schoolEntity
        every { schoolRepository.findByNameIgnoreCase(newName) } returns existingSchool

        assertThrows<IllegalArgumentException> {
            schoolService.update(schoolId, newName)
        }
    }

    @Test
    fun `should return null when updating non-existent school`() {
        val schoolId = UUID.randomUUID()
        val newName = "New Name"

        every { schoolRepository.findByIdOrNull(schoolId) } returns null

        val result = schoolService.update(schoolId, newName)

        assertNull(result)
    }

    @Test
    fun `should delete school`() {
        val schoolId = UUID.randomUUID()

        every { schoolRepository.deleteById(schoolId) } returns Unit

        schoolService.delete(schoolId)

        verify { schoolRepository.deleteById(schoolId) }
    }

    @Test
    fun `should check if school exists by name`() {
        val schoolName = "Test School"

        every { schoolRepository.existsByNameIgnoreCase(schoolName) } returns true

        val result = schoolService.exists(schoolName)

        assertTrue(result)
    }

    @Test
    fun `should list schools by ids`() {
        val school1Id = UUID.randomUUID()
        val school2Id = UUID.randomUUID()
        val school1 = SchoolEntity("School 1").apply { id = school1Id }
        val school2 = SchoolEntity("School 2").apply { id = school2Id }

        every { schoolRepository.findAllById(listOf(school1Id, school2Id)) } returns listOf(school1, school2)

        val result = schoolService.listByIds(listOf(school1Id, school2Id))

        assertEquals(2, result.size)
        assertEquals(school1Id, result[0].id)
        assertEquals(school2Id, result[1].id)
    }
}