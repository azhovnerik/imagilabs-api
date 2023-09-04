package com.anahoret.imagilabsapi.teachingmaterials.domain

import com.anahoret.imagilabsapi.classrooms.domain.ClassroomAccessService
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.*
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.security.AccessDeniedError
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionPlan.PRO
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionPlan.STANDARD
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Classroom teaching materials get use case")
class ClassroomTeachingMaterialsGetUseCaseTest {

    private val teacherBundleService = mockk<TeacherBundleService>()
    private val classroomService = mockk<ClassroomService>()
    private val classroomAccessService = mockk<ClassroomAccessService>()
    private val lessonBundleService = mockk<LessonBundleService>()
    private val teacherSubscriptionService = mockk<TeacherSubscriptionService>()

    private val classroomTeachingMaterialsGetUseCase = ClassroomTeachingMaterialsGetUseCaseImpl(
        teacherBundleService, classroomService, classroomAccessService, lessonBundleService, teacherSubscriptionService
    )

    @Test
    fun `should return access denied error when admin try to get materials`() {
        val testAdmin = testAdmin()
        val testClassroom = testClassroom()

        every { classroomService.getById(testClassroom.id) } returns testClassroom

        val result = classroomTeachingMaterialsGetUseCase.get(testAdmin, testClassroom.id)

        assertTrue(result.isLeft())
        result.onLeft { assertTrue(it is AccessDeniedError) }
    }

    @Test
    fun `should return classroom not found error`() {
        val testTeacher = testTeacher()
        val classroomId = UUID.randomUUID()

        every { classroomService.getById(classroomId) } returns null

        val result = classroomTeachingMaterialsGetUseCase.get(testTeacher, classroomId)

        assertTrue(result.isLeft())
        result.onLeft { assertTrue(it is NotFoundError) }
    }

    @Test
    fun `should return access denied error when teacher have no access to materials`() {
        val testTeacher = testTeacher()
        val testClassroom = testClassroom()

        every { classroomService.getById(testClassroom.id) } returns testClassroom
        every { classroomAccessService.canGetTeachingMaterials(testTeacher, testClassroom) } returns false

        val result = classroomTeachingMaterialsGetUseCase.get(testTeacher, testClassroom.id)

        assertTrue(result.isLeft())
        result.onLeft { assertTrue(it is AccessDeniedError) }
    }

    @Test
    fun `should return access denied error when student have no access to materials`() {
        val testClassroom = testClassroom()
        val testStudent = testStudent(testClassroom.id)

        every { classroomService.getById(testClassroom.id) } returns testClassroom
        every { classroomAccessService.canGetTeachingMaterials(testStudent, testClassroom) } returns false

        val result = classroomTeachingMaterialsGetUseCase.get(testStudent, testClassroom.id)

        assertTrue(result.isLeft())
        result.onLeft { assertTrue(it is AccessDeniedError) }
    }

    @Test
    fun `should return teaching materials for teacher with nullable pro lessons`() {
        val testTeacher = testTeacher()
        val testClassroom = testClassroom(testTeacher.id)
        val teacherBundleLessons = listOf(testBundleLesson(proLesson = true))
        val defaultBundle = testLessonBundle(default = true)

        every { classroomService.getById(testClassroom.id) } returns testClassroom
        every { classroomAccessService.canGetTeachingMaterials(testTeacher, testClassroom) } returns true
        every { lessonBundleService.getDefaultBundle() } returns defaultBundle
        every { teacherBundleService.getBundleLessonsByTeacherId(testTeacher.id) } returns teacherBundleLessons
        every { teacherSubscriptionService.getSubscriptionDto(testClassroom.teacherId) } returns testTeacherSubscription(
            STANDARD
        )

        val result = classroomTeachingMaterialsGetUseCase.get(testTeacher, testClassroom.id)

        assertTrue(result.isRight())
        result.onRight { materials ->
            assertTrue(materials.teachingSlides.any { it.path == null })
            assertTrue(materials.worksheets.any { it.path == null })
        }
    }

    @Test
    fun `should return teaching materials for student without pro lessons`() {
        val testClassroom = testClassroom()
        val testStudent = testStudent(testClassroom.id)
        val teacherBundleLessons = listOf(testBundleLesson(proLesson = true))
        val defaultBundle = testLessonBundle(default = true)

        every { classroomService.getById(testClassroom.id) } returns testClassroom
        every { classroomAccessService.canGetTeachingMaterials(testStudent, testClassroom) } returns true
        every { lessonBundleService.getDefaultBundle() } returns defaultBundle
        every { teacherBundleService.getBundleLessonsByTeacherId(testClassroom.teacherId) } returns teacherBundleLessons
        every { teacherSubscriptionService.getSubscriptionDto(testClassroom.teacherId) } returns testTeacherSubscription(
            STANDARD
        )

        val result = classroomTeachingMaterialsGetUseCase.get(testStudent, testClassroom.id)

        assertTrue(result.isRight())
        result.onRight { materials ->
            assertTrue(materials.teachingSlides.none { it.path == null })
            assertTrue(materials.worksheets.none { it.path == null })
        }
    }

    @Test
    fun `should return teaching materials for student with pro lessons`() {
        val testClassroom = testClassroom()
        val testStudent = testStudent(testClassroom.id)
        val teacherBundleLessons = listOf(testBundleLesson(proLesson = true))
        val defaultBundle = testLessonBundle(default = true)

        every { classroomService.getById(testClassroom.id) } returns testClassroom
        every { classroomAccessService.canGetTeachingMaterials(testStudent, testClassroom) } returns true
        every { lessonBundleService.getDefaultBundle() } returns defaultBundle
        every { teacherBundleService.getBundleLessonsByTeacherId(testClassroom.teacherId) } returns teacherBundleLessons
        every { teacherSubscriptionService.getSubscriptionDto(testClassroom.teacherId) } returns testTeacherSubscription(
            PRO
        )

        val result = classroomTeachingMaterialsGetUseCase.get(testStudent, testClassroom.id)

        assertTrue(result.isRight())
        result.onRight { materials ->
            assertTrue(materials.teachingSlides.any { it.proMaterial })
            assertTrue(materials.teachingSlides.none { it.path == null })
            assertTrue(materials.worksheets.any { it.proMaterial })
            assertTrue(materials.worksheets.none { it.path == null })
        }
    }
}
