package com.anahoret.imagilabsapi.teachers.export.googlesheets

import org.springframework.stereotype.Service

interface GoogleSheetsTeachersExportUseCase {

    fun export()
}

@Service
@OnGoogleSheetsIntegrationEnabled
class GoogleSheetsTeachersExportUseCaseImpl : GoogleSheetsTeachersExportUseCase {

    override fun export() {
        TODO("not implemented")
    }
}
