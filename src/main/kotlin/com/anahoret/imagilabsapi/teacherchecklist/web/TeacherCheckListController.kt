package com.anahoret.imagilabsapi.teacherchecklist.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.web.EmptySuccessResponseDto
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teacherchecklist.domain.*
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class TeacherCheckListController(
    private val getTeacherCheckListUseCase: GetTeacherCheckListUseCase,
    private val teacherProfileEntityRepository: TeacherProfileEntityRepository,
    private val completeTeacherCheckListUseCase: CompleteTeacherCheckListUseCase,
    private val teacherCheckListService: TeacherCheckListService,
    private val completeTeacherCheckListStepUseCase: CompleteTeacherCheckListStepUseCase,
    private val updateTeacherCheckListStepUseCase: UpdateTeacherCheckListStepUseCase
) {

    companion object {
        const val TEACHER_CHECK_LIST_PATH = "/api/teacher/checklist"
        const val TEACHER_CHECK_LIST_COMPLETE_STEP_PATH = "/api/teacher/checklist-step/complete"
        const val TEACHER_CHECK_LIST_COMPLETED_PATH = "/api/teacher/checklist/completed"
    }

    @Secured(UserRole.teacher)
    @GetMapping(TEACHER_CHECK_LIST_PATH)
    fun getTeacherCheckList(
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<TeacherCheckList>> {
        return when (val result = getTeacherCheckListUseCase.get(teacherProfile.id)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.teacher)
    @PutMapping(TEACHER_CHECK_LIST_COMPLETE_STEP_PATH)
    fun completeStep(
        @AuthenticationPrincipal teacherProfile: TeacherProfile,
        @RequestBody request: CompleteTeacherCheckListStepRequest
    ): ResponseEntity<ResponseDto<Void>> {
        completeTeacherCheckListStepUseCase.complete(teacherProfile.id, request.step)
        return ResponseEntity.ok(EmptySuccessResponseDto)
    }

    @Secured(UserRole.teacher)
    @PostMapping(TEACHER_CHECK_LIST_COMPLETED_PATH)
    fun completeTeacherCheckList(
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result = completeTeacherCheckListUseCase.completeTeacherCheckList(teacherProfile)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(EmptySuccessResponseDto)
        }
    }

    @Secured(UserRole.admin)
    @Deprecated("Should be deleted after first usage")
    @PostMapping(TEACHER_CHECK_LIST_PATH)
    fun createTeacherCheckLists(): ResponseEntity<*> {
        teacherProfileEntityRepository.findAll().map { createAndUpdate(it.id!!) }
        return ResponseEntity.ok("Success")
    }

    private fun createAndUpdate(teacherId: UUID): TeacherCheckList {
        val teacherChecklist = teacherCheckListService.createCheckList(teacherId)
        return updateTeacherCheckListStepUseCase.update(teacherId, teacherChecklist)
    }
}
