package com.anahoret.imagilabsapi.edlink.domain

import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.utils.CronExpressions
import com.anahoret.imagilabsapi.utils.TimeZones
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.Logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.time.measureTime

@Component
class EdLinkRefreshTeacherClassesJob(
    private val teacherProfileService: TeacherProfileService,
    private val edLinkRefreshTeacherClassesUseCase: EdLinkRefreshTeacherClassesUseCase,
    private val logger: Logger
) {

    @Scheduled(cron = CronExpressions.EVERY_NIGHT_AT_1_AM, zone = TimeZones.AMERICA_DENVER_VALUE)
    @SchedulerLock(name = "EdLinkRefreshTeacherClassesJob")
    fun refreshTeachersClasses() {
        logger.info("==========================")
        logger.info("Refreshing teacher classes")
        logger.info("==========================")
        teacherProfileService.listAllByEdLink().forEach { teacherProfile ->
            logger.info("Refreshing teacher classes for ${teacherProfile.id}")
            measureTime {
                edLinkRefreshTeacherClassesUseCase.refresh(teacherProfile)
                    .onLeft { logger.error("Refreshing teacher classes for ${teacherProfile.id} failed, $it") }
            }.also { logger.info("Refreshing teacher classes for ${teacherProfile.id} completed in $it") }
        }
        logger.info("====================================")
        logger.info("Refreshing teacher classes completed")
        logger.info("====================================")
    }

}
