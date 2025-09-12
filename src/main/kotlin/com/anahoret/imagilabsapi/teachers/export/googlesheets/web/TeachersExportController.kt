package com.anahoret.imagilabsapi.teachers.export.googlesheets.web

import com.anahoret.imagilabsapi.applicationproperties.domain.ApplicationPropertiesKey
import com.anahoret.imagilabsapi.applicationproperties.domain.ApplicationPropertiesService
import com.anahoret.imagilabsapi.common.web.EmptySuccessResponseDto
import com.anahoret.imagilabsapi.common.web.ResponseDto
import com.anahoret.imagilabsapi.common.web.SuccessResponseDto
import com.anahoret.imagilabsapi.security.UserRole
import com.anahoret.imagilabsapi.teachers.export.googlesheets.config.OnGoogleSheetsIntegrationEnabled
import com.anahoret.imagilabsapi.teachers.export.googlesheets.domain.GoogleSheetsTeachersExportUseCase
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@OnGoogleSheetsIntegrationEnabled
class TeachersExportController(
    private val googleSheetsTeachersExportUseCase: GoogleSheetsTeachersExportUseCase,
    private val applicationPropertiesService: ApplicationPropertiesService
) {

    @Secured(UserRole.ADMIN)
    @PutMapping("/api/teachers/export/google-sheet")
    fun exportGoogleSheetAll(): ResponseDto<Void> {
        googleSheetsTeachersExportUseCase.export()
        return EmptySuccessResponseDto
    }

    @Secured(UserRole.ADMIN)
    @GetMapping("/api/teachers/export/google-sheet")
    fun getGoogleSheetUri(): ResponseDto<String?> {
        return SuccessResponseDto(
            applicationPropertiesService.getPropertyOrNull(ApplicationPropertiesKey.TEACHERS_EXPORT_GOOGLE_SHEET_URI)
        )
    }

}
