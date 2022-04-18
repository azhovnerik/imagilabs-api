package com.anahoret.imagilabsapi.teachers.export.googlesheets.web

import com.anahoret.imagilabsapi.common.web.EmptySuccessResponseDto
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachers.export.googlesheets.domain.GoogleSheetsTeachersExportUseCase
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TeachersExportController(
    private val googleSheetsTeachersExportUseCase: GoogleSheetsTeachersExportUseCase
) {

    @Secured(UserRole.admin)
    @PutMapping("/api/teachers/export/google-sheet")
    fun exportGoogleSheetAll(): ResponseDto<Void> {
        googleSheetsTeachersExportUseCase.export()
        return EmptySuccessResponseDto
    }

}
