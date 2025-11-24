package com.anahoret.imagilabsapi.teachingmaterials.domain

import com.anahoret.imagilabsapi.common.*
import com.anahoret.imagilabsapi.common.domain.error.UnsupportedUserTypeError
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionPlan.PRO
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionPlan.STANDARD
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant

@DisplayName("Classroom teaching materials get use case")
class ClassroomTeachingMaterialsGetUseCaseTest {

    private val teacherBundleService = mockk<TeacherBundleService>()
    private val lessonBundleService = mockk<LessonBundleService>()
    private val teacherProfileService = mockk<TeacherProfileService>()
    private val clock = mockk<Clock>()

    private val classroomTeachingMaterialsGetUseCase = TeachingMaterialsGetUseCaseImpl(
        teacherBundleService, lessonBundleService, teacherProfileService, clock
    )

    @BeforeEach
    fun setUp() {
        every { clock.instant() } returns Instant.ofEpochMilli(500)
    }

    @Test
    fun `should return unsupported user type error when admin try to get materials`() {
        val testAdmin = testAdmin()

        val result = classroomTeachingMaterialsGetUseCase.get(testAdmin)

        assertTrue(result.isLeft())
        result.onLeft { assertTrue(it is UnsupportedUserTypeError) }
    }

    @Test
    fun `should return teaching materials for teacher with nullable pro lessons`() {
        val testTeacher = testTeacher(subscription = testTeacherSubscription(plan = PRO))
        val teacherBundleLessons = listOf(testBundleLesson(proLesson = true))
        val defaultBundle = testLessonBundle(default = true)

        every { lessonBundleService.getDefaultBundle() } returns defaultBundle
        every { teacherBundleService.getBundleLessonsByTeacherId(testTeacher.id) } returns teacherBundleLessons

        val result = classroomTeachingMaterialsGetUseCase.get(testTeacher)

        assertTrue(result.isRight())
        result.onRight { materials ->
            assertTrue(materials.teachingSlides.any { it.path == null })
            assertTrue(materials.worksheets.any { it.path == null })
        }
    }

    @Test
    fun `should return teaching materials for student without pro lessons when teacher have standard subscription`() {
        val testStudent = testStudent()
        val teacherBundleLessons = listOf(testBundleLesson(proLesson = true))
        val defaultBundle = testLessonBundle(default = true)
        val teacher = testTeacher(subscription = testTeacherSubscription(plan = STANDARD))

        every { lessonBundleService.getDefaultBundle() } returns defaultBundle
        every { teacherBundleService.getBundleLessonsByTeacherId(teacher.id) } returns teacherBundleLessons
        every { teacherProfileService.listTeachersByStudent(testStudent.id) } returns listOf(teacher)

        val result = classroomTeachingMaterialsGetUseCase.get(testStudent)

        assertTrue(result.isRight())
        result.onRight { materials ->
            assertTrue(materials.teachingSlides.none { it.path == null })
            assertTrue(materials.worksheets.none { it.path == null })
        }
    }

    @Test
    fun `should return teaching materials for student with pro lessons when teacher have pro subscription`() {
        val testStudent = testStudent()
        val teacherBundleLessons = listOf(testBundleLesson(proLesson = true))
        val defaultBundle = testLessonBundle(default = true)
        val teacher = testTeacher(subscription = testTeacherSubscription(start = 0, end = 1000, PRO))

        every { lessonBundleService.getDefaultBundle() } returns defaultBundle
        every { teacherBundleService.getBundleLessonsByTeacherId(teacher.id) } returns teacherBundleLessons
        every { teacherProfileService.listTeachersByStudent(testStudent.id) } returns listOf(teacher)

        val result = classroomTeachingMaterialsGetUseCase.get(testStudent)

        assertTrue(result.isRight())
        result.onRight { materials ->
            assertTrue(materials.teachingSlides.any { it.proMaterial })
            assertTrue(materials.teachingSlides.none { it.path == null })
            assertTrue(materials.worksheets.any { it.proMaterial })
            assertTrue(materials.worksheets.none { it.path == null })
        }
    }
}
