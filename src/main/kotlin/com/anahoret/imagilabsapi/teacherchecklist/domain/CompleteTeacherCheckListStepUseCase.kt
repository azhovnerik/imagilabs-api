package com.anahoret.imagilabsapi.teacherchecklist.domain

import com.anahoret.imagilabsapi.teacherchecklist.storage.TeacherCheckListStep
import com.anahoret.imagilabsapi.teachers.export.clevertap.domain.ClevertapSendAnalyticsUseCase
import org.springframework.stereotype.Service
import java.util.*

interface CompleteTeacherCheckListStepUseCase {

    fun complete(teacherId: UUID, step: TeacherCheckListStep): TeacherCheckList
}

@Service
class CompleteTeacherCheckListStepUseCaseImpl(
    private val teacherCheckListService: TeacherCheckListService,
    private val clevertapSendAnalyticsUseCase: ClevertapSendAnalyticsUseCase?
): CompleteTeacherCheckListStepUseCase {

    override fun complete(teacherId: UUID, step: TeacherCheckListStep): TeacherCheckList {
        teacherCheckListService.completeCheckListStep(teacherId, step)
        clevertapSendAnalyticsUseCase?.sendCompleteOnboardingStepEvent(step)
        return teacherCheckListService.getCheckList(teacherId)
    }
}
