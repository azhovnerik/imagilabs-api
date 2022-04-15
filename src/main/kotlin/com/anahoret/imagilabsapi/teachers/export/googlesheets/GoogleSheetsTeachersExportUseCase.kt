package com.anahoret.imagilabsapi.teachers.export.googlesheets

import com.anahoret.imagilabsapi.applicationproperties.domain.ApplicationPropertiesKey
import com.anahoret.imagilabsapi.applicationproperties.domain.ApplicationPropertiesService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileAdminView
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.utils.DateUtils
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter
import java.util.*

interface GoogleSheetsTeachersExportUseCase {

    fun export()
}

@Service
@OnGoogleSheetsIntegrationEnabled
class GoogleSheetsTeachersExportUseCaseImpl(
    private val applicationPropertiesService: ApplicationPropertiesService,
    private val googleSheetApi: GoogleSheetApi,
    private val teacherProfileService: TeacherProfileService
) : GoogleSheetsTeachersExportUseCase {

    companion object {

        val DATE_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }

    override fun export() {
        val sheetId = applicationPropertiesService.getProperty(ApplicationPropertiesKey.TEACHERS_EXPORT_GOOGLE_SHEET_ID)
        val sheetName =
            applicationPropertiesService.getProperty(ApplicationPropertiesKey.TEACHERS_EXPORT_GOOGLE_SHEET_NAME)
        val existingTeacherIds = googleSheetApi.getSheet(sheetId, EntireSheetRange(sheetName))
            .map { it.first() }
            .map(UUID::fromString)

        val teachers = teacherProfileService.listForAdmin(existingTeacherIds, Sort.by("id"))
        if (teachers.isEmpty()) return
        val cells = toCells(teachers)

        val cellRange = CellRange(
            sheetName = sheetName,
            startRow = existingTeacherIds.size + 1,
            startCol = 1,
            endRow = existingTeacherIds.size + cells.size,
            endCol = cells.first().size
        )
        googleSheetApi.updateSheet(sheetId, cellRange, cells)
    }

    private fun toCells(teachers: List<TeacherProfileAdminView>): List<List<String>> {
        return teachers.map {
            with(it) {
                val registrationDateTime = DateUtils.toStockholmDateTime(createdAt)
                    .format(DATE_TIME_FORMAT)
                listOf(
                    id.toString(),
                    email,
                    "$firstName $lastName",
                    country,
                    organization,
                    howDidYouHearAboutUs,
                    registrationDateTime
                )
            }
        }
    }
}
