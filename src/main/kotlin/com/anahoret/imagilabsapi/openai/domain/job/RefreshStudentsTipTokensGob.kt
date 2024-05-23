package com.anahoret.imagilabsapi.openai.domain.job

import com.anahoret.imagilabsapi.openai.domain.TipTokensService
import com.anahoret.imagilabsapi.utils.CronExpressions
import com.anahoret.imagilabsapi.utils.TimeZones
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("prod", "stage")
class RefreshStudentsTipTokensGob(
    private val tipTokensService: TipTokensService
) {
    @Scheduled(cron = CronExpressions.EVERY_HOUR, zone = TimeZones.EUROPE_STOCKHOLM_VALUE)
    @SchedulerLock(name = "RefreshStudentsTipTokensGob")
    fun refreshTipTokens() {
        tipTokensService.refreshTipTokens()
    }
}
