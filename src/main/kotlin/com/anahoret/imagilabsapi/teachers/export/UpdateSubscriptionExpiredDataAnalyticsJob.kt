package com.anahoret.imagilabsapi.teachers.export

import com.anahoret.imagilabsapi.teachers.export.clevertap.domain.ClevertapSendAnalyticsUseCase
import com.anahoret.imagilabsapi.teachers.export.googlesheets.domain.GetTeachersWithExpiredSubscriptionUseCase
import com.anahoret.imagilabsapi.teachers.export.googlesheets.domain.GoogleSheetsTeachersExportUseCase
import com.anahoret.imagilabsapi.utils.CronExpressions
import com.anahoret.imagilabsapi.utils.TimeZones
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("prod", "stage")
class UpdateSubscriptionExpiredDataAnalyticsJob(
    private val clevertapSendAnalyticsUseCase: ClevertapSendAnalyticsUseCase,
    private val googleSheetsTeachersExportUseCase: GoogleSheetsTeachersExportUseCase,
    private val getTeachersWithExpiredSubscriptionUseCase: GetTeachersWithExpiredSubscriptionUseCase
) {

    @Scheduled(cron = CronExpressions.EVERY_NIGHT_AT_1_AM, zone = TimeZones.EUROPE_STOCKHOLM_VALUE)
    @SchedulerLock(name = "UpdateSubscriptionExpiredDataAnalyticsJob")
    fun updateSubscriptionExpiredData() {
        val teachersIdsToUpdate = getTeachersWithExpiredSubscriptionUseCase.getAll()
        if (teachersIdsToUpdate.isNotEmpty()) {
            googleSheetsTeachersExportUseCase.updateAsync(teachersIdsToUpdate)
            clevertapSendAnalyticsUseCase.sendTeachersExpiredSubscriptionEvent(teachersIdsToUpdate)
        }
    }
}
