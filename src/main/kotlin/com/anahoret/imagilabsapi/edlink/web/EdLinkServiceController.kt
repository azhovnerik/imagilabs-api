package com.anahoret.imagilabsapi.edlink.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.web.EmptySuccessResponseDto
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.edlink.domain.EdLinkRefreshTeacherClassesUseCase
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@Secured(UserRole.ADMIN)
class EdLinkServiceController(
    private val edLinkRefreshTeacherClassesUseCase: EdLinkRefreshTeacherClassesUseCase,
    private val teacherProfileService: TeacherProfileService
) {

    @PostMapping("/api/edlink/refresh-teacher-classes")
    fun refreshTeacherClasses(
        @RequestBody request: RefreshTeacherClassesRequest
    ): ResponseEntity<ResponseDto<Void>> {
        val teacherProfile = request.email?.let(teacherProfileService::getTeacherByEmail)
            ?: request.id?.let(teacherProfileService::getTeacherById)
            ?: return mapErrors(NotFoundError("TEACHER_NOT_FOUND"))
        return when (val result = edLinkRefreshTeacherClassesUseCase.refresh(teacherProfile)) {
            is Either.Right -> ResponseEntity.ok(EmptySuccessResponseDto)
            is Either.Left -> mapErrors(result.value)
        }
    }

    class RefreshTeacherClassesRequest(
        val email: String?,
        val id: UUID?
    )

}


