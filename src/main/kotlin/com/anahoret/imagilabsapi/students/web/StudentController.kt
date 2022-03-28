package com.anahoret.imagilabsapi.students.web

import arrow.core.Either
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.mapErrors
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.students.domain.StudentDeleteUseCase
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class StudentController(
    private val studentDeleteUseCase: StudentDeleteUseCase
) {

    @Secured(UserRole.teacher)
    @DeleteMapping("/api/students/{studentId}")
    fun deleteStudent(
        @PathVariable studentId: UUID,
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result = studentDeleteUseCase.delete(teacherProfile, studentId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok().build()
        }
    }

}
