package com.anahoret.imagilabsapi.common

import com.anahoret.imagilabsapi.admins.domain.AdminProfile
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomPermissions
import com.anahoret.imagilabsapi.classrooms.domain.TeacherRole
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscription
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionPlan
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionPlan.STANDARD
import com.anahoret.imagilabsapi.teacherchecklist.domain.CheckListStep
import com.anahoret.imagilabsapi.teacherchecklist.domain.TeacherCheckList
import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep.CREATE_OR_JOIN_YOUR_FIRST_CLASSROOM
import com.anahoret.imagilabsapi.teachers.domain.GradeLevel
import com.anahoret.imagilabsapi.teachers.domain.SchoolRole
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachingmaterials.domain.BundleLesson
import com.anahoret.imagilabsapi.teachingmaterials.domain.LessonBundle
import com.anahoret.imagilabsapi.tweets.domain.Tweet
import org.apache.commons.lang3.RandomStringUtils.secure
import java.util.*

fun testAdmin(): AdminProfile {
    return AdminProfile(UUID.randomUUID(), "Admin")
}

fun testTeacher(
    teacherId: UUID = UUID.randomUUID(),
    subscription: TeacherSubscription = TeacherSubscription(null, null, STANDARD, false, teacherId)
): TeacherProfile {
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
        subscription = subscription
    )
}

fun testCompleteTeacherProfile(
    teacherId: UUID = UUID.randomUUID(),
    firstName: String = "John",
    lastName: String = "Doe",
    email: String = "john.doe@example.com",
    country: String = "USA",
    organization: String = "Test School",
    state: String? = "California",
    schoolRoles: List<SchoolRole> = listOf(SchoolRole.TEACHER),
    grades: List<GradeLevel> = listOf(GradeLevel.NINTH_GRADE),
    subjects: String? = "Math, Science",
    schools: String? = "Test High School",
    subscription: TeacherSubscription = TeacherSubscription(null, null, STANDARD, false, teacherId)
): TeacherProfile {
    return TeacherProfile(
        id = teacherId,
        firstName = firstName,
        lastName = lastName,
        email = email,
        country = country,
        organization = organization,
        createdAt = 0L,
        emailVerified = true,
        marketingEmailSubscribed = false,
        subscription = subscription,
        state = state,
        schoolRoles = schoolRoles,
        grades = grades,
        subjects = subjects,
        schools = schools
    )
}

fun testTeacherSubscription(
    start: Long = 0L,
    end: Long = 100L,
    plan: TeacherSubscriptionPlan = STANDARD,
    canceled: Boolean = false,
    teacherId: UUID = UUID.randomUUID()
): TeacherSubscription {
    return TeacherSubscription(start, end, plan, canceled, teacherId)
}

fun testStudent(): StudentProfile {
    return StudentProfile(
        UUID.randomUUID(),
        "Martin",
        "mrtinos",
        createdAt = 0L
    )
}

fun testClassroom(
    id: UUID = UUID.randomUUID(),
    name: String = "Test classroom",
    accessCode: String = "1111",
    studentsCount: Long = 5,
    projectsCount: Long = 5,
    teacherId: UUID = UUID.randomUUID(),
    teachersCount: Long = 2,
    blocked: Boolean = false,
    permissions: ClassroomPermissions = ClassroomPermissions(canManageCoTeachers = true),
    teacherRole: TeacherRole = TeacherRole.OWNER,
    deleted: Boolean = false
): Classroom {
    return Classroom(
        id = id,
        name = name,
        accessCode = accessCode,
        studentsCount = studentsCount,
        projectsCount = projectsCount,
        teacherId = teacherId,
        teachersCount = teachersCount,
        blocked = blocked,
        permissions = permissions,
        schoolName = null,
        teacherRole = teacherRole,
        deleted = deleted
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
        name = secure().nextAlphabetic(6),
        worksheetUri = "http://${secure().nextAlphabetic(4)}.com",
        slidesUri = "http://${secure().nextAlphabetic(4)}.com",
        proLesson = proLesson
    )
}

fun testLessonBundle(default: Boolean = false, lessons: List<BundleLesson>? = null): LessonBundle {
    val bundleId = UUID.randomUUID()
    val bundleLessons = lessons ?: listOf(testBundleLesson(bundleId))
    return LessonBundle(
        id = bundleId,
        name = secure().nextAlphabetic(6),
        defaultBundle = default,
        lessons = bundleLessons
    )
}
