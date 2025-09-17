package com.anahoret.imagilabsapi.lovable.domain

import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service

interface GetLovableAccountForTeacherUseCase {
    fun get(teacherProfile: TeacherProfile): LovableAccount?
}

@Service
class GetLovableAccountForTeacherUseCaseImpl(
    private val lovableAccountService: LovableAccountService
) : GetLovableAccountForTeacherUseCase {
    override fun get(teacherProfile: TeacherProfile): LovableAccount? {
        return lovableAccountService.getActive(teacherProfile)
    }

}
