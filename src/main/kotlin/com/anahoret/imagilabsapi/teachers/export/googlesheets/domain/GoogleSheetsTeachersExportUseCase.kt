package com.anahoret.imagilabsapi.teachers.export.googlesheets.domain

import com.anahoret.imagilabsapi.applicationproperties.domain.ApplicationPropertiesKey
import com.anahoret.imagilabsapi.applicationproperties.domain.ApplicationPropertiesService
import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscription
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileAdminView
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.teachers.export.googlesheets.api.CellRange
import com.anahoret.imagilabsapi.teachers.export.googlesheets.api.EntireSheetRange
import com.anahoret.imagilabsapi.teachers.export.googlesheets.api.GoogleSheetApi
import com.anahoret.imagilabsapi.teachers.export.googlesheets.config.OnGoogleSheetsIntegrationEnabled
import com.anahoret.imagilabsapi.utils.DateUtils
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.format.DateTimeFormatter
import java.util.*

interface GoogleSheetsTeachersExportUseCase {

    fun export()
    fun exportAsync(teacherId: UUID)
    fun updateAsync(teacherId: UUID)
    fun updateAsync(teacherIds: List<UUID>)
}

@Service
@OnGoogleSheetsIntegrationEnabled
class GoogleSheetsTeachersExportUseCaseImpl(
    private val applicationPropertiesService: ApplicationPropertiesService,
    private val googleSheetApi: GoogleSheetApi,
    private val teacherProfileService: TeacherProfileService,
    private val clock: Clock
) : GoogleSheetsTeachersExportUseCase {

    companion object {

        val DATE_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }

    override fun export() {
        doExport { rows ->
            val existingTeacherIds = rows
                .drop(1) // Skip header
                .filter(List<String>::isNotEmpty)
                .map(List<String>::first)
                .map(UUID::fromString)
            teacherProfileService.listForAdmin(existingTeacherIds, Sort.by("id"))
        }
    }

    @Async
    override fun exportAsync(teacherId: UUID) {
        teacherProfileService.getTeacherByIdForAdmin(teacherId)
            ?.let { teacherProfile -> doExport { listOf(teacherProfile) } }
    }

    @Async
    override fun updateAsync(teacherId: UUID) {
        teacherProfileService.getTeacherByIdForAdmin(teacherId)
            ?.let { teacherProfile -> doUpdate { listOf(teacherProfile) } }
    }

    override fun updateAsync(teacherIds: List<UUID>) {
        teacherProfileService.getTeachersByIdsForAdmin(teacherIds)
            .filterNotNull()
            .map { teacherProfile -> doUpdate { listOf(teacherProfile) } }
    }

    private fun doExport(getTeachers: (List<List<String>>) -> List<TeacherProfileAdminView>) {
        val sheetId = applicationPropertiesService.getProperty(ApplicationPropertiesKey.TEACHERS_EXPORT_GOOGLE_SHEET_ID)
        val sheetName =
            applicationPropertiesService.getProperty(ApplicationPropertiesKey.TEACHERS_EXPORT_GOOGLE_SHEET_NAME)
        val rows = googleSheetApi.getSheet(sheetId, EntireSheetRange(sheetName))

        val teachers = getTeachers(rows)
        if (teachers.isEmpty()) return

        val cells = toCells(teachers)
        val cellRange = CellRange(
            sheetName = sheetName,
            startRow = rows.size + 1,
            startCol = 1,
            endRow = rows.size + cells.size,
            endCol = cells.first().size
        )
        googleSheetApi.updateSheet(sheetId, cellRange, cells)
    }

    private fun doUpdate(getTeachers: (List<List<String>>) -> List<TeacherProfileAdminView>) {
        val sheetId = applicationPropertiesService.getProperty(ApplicationPropertiesKey.TEACHERS_EXPORT_GOOGLE_SHEET_ID)
        val sheetName =
            applicationPropertiesService.getProperty(ApplicationPropertiesKey.TEACHERS_EXPORT_GOOGLE_SHEET_NAME)
        val rows = googleSheetApi.getSheet(sheetId, EntireSheetRange(sheetName))

        val teachers = getTeachers(rows)

        val teacherRowIndex = rows.indexOfFirst { row -> row[0] == teachers[0].id.toString() } + 1

        if (teacherRowIndex == 0) return

        val cells = toCells(teachers)
        val cellRange = CellRange(
            sheetName = sheetName,
            startRow = teacherRowIndex,
            startCol = 1,
            endRow = teacherRowIndex,
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
                    howDidYouHearAboutUsOther ?: "",
                    marketingEmailSubscribed.toString(),
                    registrationDateTime,
                    subscription.status(),
                    subscription.start.toStockholmDateTime(),
                    subscription.end.toStockholmDateTime()
                )
            }
        }
    }

    private fun Long?.toStockholmDateTime(): String {
        return when(this) {
            null -> "null"
            else -> DateUtils.toStockholmDateTime(this)
                .format(DATE_TIME_FORMAT)
        }
    }

    private fun TeacherSubscription.status(): String {
        if (start == null || end == null) return "inactive"
        if (canceled) return "canceled"
        if (end < clock.instant().toEpochMilli()) return "expired"

        return "active"
    }
}
