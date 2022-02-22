package com.anahoret.imagilabsapi.classrooms.web

import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomCreateRequest
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class ClassroomController {

    @Secured(UserRole.teacher)
    @PostMapping("/api/classrooms")
    fun createClassroom(
        @RequestBody classroomCreateRequest: ClassroomCreateRequest,
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<Classroom?>> {
        return ResponseEntity.ok(SuccessResponseDto(Classroom(UUID.randomUUID(), classroomCreateRequest.name)))
    }

}
