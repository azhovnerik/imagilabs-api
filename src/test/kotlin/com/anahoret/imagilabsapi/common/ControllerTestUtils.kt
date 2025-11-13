package com.anahoret.imagilabsapi.common

import com.anahoret.imagilabsapi.admins.domain.AdminProfile
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomPermissions
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscription
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionPlan
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionPlan.STANDARD
import com.anahoret.imagilabsapi.teacherchecklist.domain.CheckListStep
import com.anahoret.imagilabsapi.teacherchecklist.domain.TeacherCheckList
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep.CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachingmaterials.domain.BundleLesson
import com.anahoret.imagilabsapi.teachingmaterials.domain.LessonBundle
import com.anahoret.imagilabsapi.tweets.domain.Tweet
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import java.util.*

fun testAdmin(): AdminProfile {
    return AdminProfile(UUID.randomUUID(), "Admin")
}

fun testTeacher(): TeacherProfile {
    val teacherId = UUID.randomUUID()
    return TeacherProfile(
        id = teacherId,
        firstName = "John",
        lastName = "Snow",
        email = "johnsnow@winterfell.com",
        country = "Westeros",
        organization = "Starks",
        createdAt = 0L,
        emailVerified = true,
        marketingEmailSubscribed = false,
        subscription = TeacherSubscription(null, null, STANDARD, false, teacherId),
    )
}

fun testTeacherSubscription(
    plan: TeacherSubscriptionPlan = STANDARD,
    canceled: Boolean = false,
    teacherId: UUID = UUID.randomUUID()
): TeacherSubscription {
    return TeacherSubscription(0, 100, plan, canceled, teacherId)
}

fun testStudent(): StudentProfile {
    return StudentProfile(
        UUID.randomUUID(),
        "Martin",
        "mrtinos",
        createdAt = 0L
    )
}

fun testClassroom(teacherId: UUID = UUID.randomUUID()): Classroom {
    return Classroom(
        UUID.randomUUID(),
        "Test classroom",
        "1111",
        5L,
        5L,
        teacherId,
        2,
        blocked = false,
        ClassroomPermissions(true)
    )
}

fun testTweet(): Tweet {
    return Tweet(1, "twitter.com")
}

fun testTeacherCheckList(
    checkListStep: CheckListStep = CheckListStep(
        step = CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM,
        completed = true
    )
): TeacherCheckList {
    return TeacherCheckList(checkListSteps = listOf(checkListStep))
}

fun testBundleLesson(bundleId: UUID = UUID.randomUUID(), proLesson: Boolean = false): BundleLesson {
    return BundleLesson(
        id = UUID.randomUUID(),
        bundleId = bundleId,
        index = -1,
        name = randomAlphabetic(6),
        worksheetUri = "http://${randomAlphabetic(4)}.com",
        slidesUri = "http://${randomAlphabetic(4)}.com",
        proLesson = proLesson
    )
}

fun testLessonBundle(default: Boolean = false, lessons: List<BundleLesson>? = null): LessonBundle {
    val bundleId = UUID.randomUUID()
    val bundleLessons = lessons ?: listOf(testBundleLesson(bundleId))
    return LessonBundle(
        id = bundleId,
        name = randomAlphabetic(6),
        defaultBundle = default,
        lessons = bundleLessons
    )
}
