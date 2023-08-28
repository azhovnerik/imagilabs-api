package com.anahoret.imagilabsapi.teachingmaterials.domain

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.validation.ValidationError
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Teacher bundle use case")
class TeacherBundleUseCaseImplTest {

    private val teacherBundleService = mockk<TeacherBundleService>()
    private val teacherProfileService = mockk<TeacherProfileService>()
    private val lessonBundleService = mockk<LessonBundleService>()
    private val teacherBundleCreateUseCase =
        TeacherBundleCreateUseCaseImpl(teacherBundleService, teacherProfileService, lessonBundleService)

    private val teacherId = UUID.randomUUID()
    private val bundleId = UUID.randomUUID()

    @Test
    fun `should return not found error when teacher not found`() {
        every { teacherProfileService.exists(teacherId) } returns false

        when (val result = teacherBundleCreateUseCase.create(teacherId, bundleId)) {
            is Either.Left -> assertTrue(result.value is NotFoundError)
            is Either.Right -> fail()
        }
    }

    @Test
    fun `should return not found bundle`() {
        every { teacherProfileService.exists(teacherId) } returns true
        every { lessonBundleService.exists(bundleId) } returns false

        when (val result = teacherBundleCreateUseCase.create(teacherId, bundleId)) {
            is Either.Left -> assertTrue(result.value is NotFoundError)
            is Either.Right -> fail()
        }
    }

    @Test
    fun `should return validation error when bundle is default`() {
        every { teacherProfileService.exists(teacherId) } returns true
        every { lessonBundleService.exists(bundleId) } returns true
        every { lessonBundleService.isDefault(bundleId) } returns true

        when (val result = teacherBundleCreateUseCase.create(teacherId, bundleId)) {
            is Either.Left -> assertTrue(result.value is ValidationError)
            is Either.Right -> fail()
        }
    }

    @Test
    fun `should return validation error when bundle has already linked to teacher`() {
        every { teacherProfileService.exists(teacherId) } returns true
        every { lessonBundleService.exists(bundleId) } returns true
        every { lessonBundleService.isDefault(bundleId) } returns false
        every { teacherBundleService.hasLinkedBundle(teacherId, bundleId) } returns true

        when (val result = teacherBundleCreateUseCase.create(teacherId, bundleId)) {
            is Either.Left -> assertTrue(result.value is ValidationError)
            is Either.Right -> fail()
        }
    }

    @Test
    fun `should return unit when bundle was added successfully`() {
        every { teacherProfileService.exists(teacherId) } returns true
        every { lessonBundleService.exists(bundleId) } returns true
        every { lessonBundleService.isDefault(bundleId) } returns false
        every { teacherBundleService.hasLinkedBundle(teacherId, bundleId) } returns false
        every { teacherBundleService.create(teacherId, bundleId) } returns Unit

        when (teacherBundleCreateUseCase.create(teacherId, bundleId)) {
            is Either.Left -> fail()
            is Either.Right -> verify { teacherBundleService.create(teacherId, bundleId) }
        }
    }

}
