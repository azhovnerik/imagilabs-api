package com.anahoret.imagilabsapi.schools.web

import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.schools.domain.School
import com.anahoret.imagilabsapi.schools.domain.SchoolService
import com.anahoret.imagilabsapi.security.UserRole
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/admin/schools")
@Secured(UserRole.ADMIN)
class SchoolController(
    private val schoolService: SchoolService
) {

    @PostMapping
    fun createSchool(@RequestBody request: SchoolCreateRequest): ResponseEntity<ResponseDto<School>> {
        return try {
            val school = schoolService.create(request.name)
            ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponseDto(school))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(SuccessResponseDto(null))
        }
    }

    @GetMapping
    fun listSchools(): SuccessResponseDto<List<School>> {
        val schools = schoolService.listAll(Sort.by(Sort.Direction.ASC, "name"))
        return SuccessResponseDto(schools)
    }

    @GetMapping("/{id}")
    fun getSchool(@PathVariable id: UUID): ResponseEntity<ResponseDto<School>> {
        val school = schoolService.getById(id)
        return if (school != null) {
            ResponseEntity.ok(SuccessResponseDto(school))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PutMapping("/{id}")
    fun updateSchool(
        @PathVariable id: UUID,
        @RequestBody request: SchoolUpdateRequest
    ): ResponseEntity<ResponseDto<School>> {
        return try {
            val school = schoolService.update(id, request.name)
            if (school != null) {
                ResponseEntity.ok(SuccessResponseDto(school))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(SuccessResponseDto(null))
        }
    }

    @DeleteMapping("/{id}")
    fun deleteSchool(@PathVariable id: UUID): ResponseEntity<Void> {
        schoolService.delete(id)
        return ResponseEntity.noContent().build()
    }
}

data class SchoolCreateRequest(
    val name: String
)

data class SchoolUpdateRequest(
    val name: String
)