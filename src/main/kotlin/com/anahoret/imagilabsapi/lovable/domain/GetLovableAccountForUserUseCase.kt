package com.anahoret.imagilabsapi.lovable.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import org.springframework.stereotype.Service
import java.util.*

interface GetLovableAccountForUserUseCase {
    fun get(userProfile: UserProfile, classroomId: UUID? = null): LovableAccount?
}

@Service
class GetLovableAccountForUserUseCaseImpl(
    private val lovableAccountService: LovableAccountService,
    private val lovableClassroomService: LovableClassroomService
) : GetLovableAccountForUserUseCase {
    override fun get(userProfile: UserProfile, classroomId: UUID?): LovableAccount? {
        if (userProfile is StudentProfile) {
            if (classroomId == null) return null
            val lovableClassroom = lovableClassroomService.getIntegrationForClassroom(classroomId)
                ?: return null
            if (lovableClassroom.lovableIntegrationPaused || !lovableClassroom.lovableIntegrationEnabled) {
                return null
            }
        }
        return lovableAccountService.getActive(userProfile)
    }

}
