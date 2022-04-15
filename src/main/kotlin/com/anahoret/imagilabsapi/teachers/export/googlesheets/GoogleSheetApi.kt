package com.anahoret.imagilabsapi.teachers.export.googlesheets

import org.springframework.stereotype.Service

interface GoogleSheetApi {
}

@Service
@OnGoogleSheetsIntegrationEnabled
class GoogleSheetApiImpl : GoogleSheetApi {
}
