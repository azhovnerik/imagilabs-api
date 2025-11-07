package com.anahoret.imagilabsapi.lovable.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import org.springframework.stereotype.Service

interface GetLovableAccountForUserUseCase {
    fun get(userProfile: UserProfile): LovableAccount?
}

@Service
class GetLovableAccountForUserUseCaseImpl(
    private val lovableAccountService: LovableAccountService,
    private val lovableClassroomService: LovableClassroomService
) : GetLovableAccountForUserUseCase {
    override fun get(userProfile: UserProfile): LovableAccount? {
        if (userProfile is StudentProfile) {
            val lovableClassroom = lovableClassroomService.getIntegrationForClassroom(userProfile.classroomId)
                ?: return null
            if (lovableClassroom.lovableIntegrationPaused || !lovableClassroom.lovableIntegrationEnabled) {
                return null
            }
        }
        return lovableAccountService.getActive(userProfile)
    }

}
