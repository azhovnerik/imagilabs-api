package com.anahoret.imagilabsapi.userclassroomlink.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionPlan
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service
import java.util.*

interface CoTeacherClassroomLinkService {

    fun isLinkedToClassroom(classroomId: UUID, userProfile: UserProfile): Boolean
}

@Service
class CoTeacherClassroomLinkServiceImpl(
    private val teacherProfileService: TeacherProfileService,
    private val coTeacherService: CoTeacherService
): CoTeacherClassroomLinkService {

    override fun isLinkedToClassroom(classroomId: UUID, userProfile: UserProfile): Boolean {
        return when (userProfile.userType) {
            UserType.TEACHER -> {
                val teacherProfile = teacherProfileService.getTeacherById(userProfile.id)
                        ?: return false

                coTeacherService.isLinkedToClassroom(classroomId, teacherProfile.id)
                        && teacherProfile.subscription.plan == TeacherSubscriptionPlan.PRO
            }
            else -> false
        }
    }
}
