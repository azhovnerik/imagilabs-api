package com.anahoret.imagilabsapi.classrooms.web

import arrow.core.Either
import com.anahoret.imagilabsapi.classrooms.domain.*
import com.anahoret.imagilabsapi.classrooms.domain.studentcredentialscards.StudentCredentialsCardsGenerator
import com.anahoret.imagilabsapi.classrooms.domain.studentcredentialscards.StudentsCredentialsCardsFormat
import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.common.web.*
import com.anahoret.imagilabsapi.projects.domain.ProjectCard
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.students.domain.StudentClassroomCard
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.data.domain.Sort
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class ClassroomController(
    private val classroomCreateUseCase: ClassroomCreateUseCase,
    private val classroomGetUseCase: ClassroomGetUseCase,
    private val classroomService: ClassroomService,
    private val listStudentsInClassroomUseCase: ListStudentsInClassroomUseCase,
    private val listProjectsInClassroomUseCase: ListProjectsInClassroomUseCase,
    private val studentCredentialsCardsGenerator: StudentCredentialsCardsGenerator,
    private val classroomDeleteUseCase: ClassroomDeleteUseCase,
    private val classroomUpdateUseCase: ClassroomUpdateUseCase
) {

    @Secured(UserRole.teacher)
    @PostMapping("/api/classrooms")
    fun createClassroom(
        @RequestBody classroomCreateRequest: ClassroomCreateRequest,
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<Classroom?>> {
        return when (val createResult = classroomCreateUseCase.create(teacherProfile, classroomCreateRequest)) {
            is Either.Left -> createResult.value.toBadRequestResponse()
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(createResult.value))
        }
    }

    @Secured(UserRole.teacher)
    @GetMapping("/api/classrooms")
    fun listClassrooms(@AuthenticationPrincipal teacherProfile: TeacherProfile): ResponseDto<List<Classroom>> {
        return SuccessResponseDto(classroomService.listByTeacher(teacherProfile.id))
    }

    @Secured(UserRole.teacher, UserRole.student)
    @GetMapping("/api/classrooms/{classroomId}")
    fun getClassroomById(
        @PathVariable classroomId: UUID,
        @AuthenticationPrincipal userProfile: UserProfile
    ): ResponseEntity<ResponseDto<Classroom?>> {
        return when (val result = classroomGetUseCase.get(userProfile, classroomId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.teacher)
    @PatchMapping("/api/classrooms/{classroomId}")
    fun updateClassroom(
        @PathVariable classroomId: UUID,
        @RequestBody classroomUpdateRequest: ClassroomUpdateRequest,
        @AuthenticationPrincipal teacherProfile: TeacherProfile,
    ): ResponseEntity<ResponseDto<Classroom?>> {
        return when (val result = classroomUpdateUseCase.update(teacherProfile, classroomId, classroomUpdateRequest)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.teacher)
    @DeleteMapping("/api/classrooms/{classroomId}")
    fun deleteClassroom(
        @PathVariable classroomId: UUID,
        @AuthenticationPrincipal teacherProfile: TeacherProfile
    ): ResponseEntity<ResponseDto<Void>> {
        return when (val result = classroomDeleteUseCase.delete(teacherProfile, classroomId)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok().build()
        }
    }

    @Secured(UserRole.teacher)
    @GetMapping("/api/classrooms/{classroomId}/student-classroom-cards")
    fun listStudentsCredentialsCardsInClassroom(
        @PathVariable classroomId: UUID,
        @RequestParam(required = false) searchQuery: String?,
        @AuthenticationPrincipal teacherProfile: TeacherProfile,
        sort: Sort
    ): ResponseEntity<ResponseDto<List<StudentClassroomCard>?>> {
        return when (val result = listStudentsInClassroomUseCase.list(
            teacherProfile, classroomId, searchQuery, sort
        )) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

    @Secured(UserRole.teacher)
    @PostMapping("/api/classrooms/{classroomId}/student-classroom-cards/download")
    fun downloadStudentsCredentialsCardsInClassroomByIds(
        @PathVariable classroomId: UUID,
        @AuthenticationPrincipal teacherProfile: TeacherProfile,
        @RequestBody downloadRequest: DownloadStudentsCredentialsRequest
    ): ResponseEntity<*> {
        return when (val result = studentCredentialsCardsGenerator.generate(
            teacherProfile, classroomId, downloadRequest
        )) {
            is Either.Left -> mapErrors<Void>(result.value)
            is Either.Right -> result.value.inputStream.toFileResponse(
                fileName = result.value.fileName,
                mediaType = result.value.format.toMediaType()
            )
        }
    }

    @Secured(UserRole.teacher, UserRole.student)
    @PostMapping("/api/classrooms/{classroomId}/projects/search")
    fun listProjectsInClassroom(
        @PathVariable classroomId: UUID,
        @RequestBody searchRequest: ClassroomSearchProjectsRequest,
        @AuthenticationPrincipal userProfile: UserProfile,
        sort: Sort
    ): ResponseEntity<ResponseDto<List<ProjectCard>>> {
        return when (val result =
            listProjectsInClassroomUseCase.list(userProfile, classroomId, searchRequest, sort)) {
            is Either.Left -> mapErrors(result.value)
            is Either.Right -> ResponseEntity.ok(SuccessResponseDto(result.value))
        }
    }

}

private fun StudentsCredentialsCardsFormat.toMediaType(): MediaType {
    return when (this) {
        StudentsCredentialsCardsFormat.PDF -> MediaType.APPLICATION_PDF
        StudentsCredentialsCardsFormat.CSV -> MediaType.parseMediaType("text/csv")
    }
}
