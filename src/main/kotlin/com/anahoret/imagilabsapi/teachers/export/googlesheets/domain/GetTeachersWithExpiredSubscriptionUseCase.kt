package com.anahoret.imagilabsapi.teachers.export.googlesheets.domain

import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.utils.DateUtils
import org.springframework.stereotype.Service
import java.time.Clock
import java.util.*

interface GetTeachersWithExpiredSubscriptionUseCase {

    fun getAll(): List<UUID>
}

@Service
class GetTeachersWithExpiredSubscriptionUseCaseImpl(
    private val teacherProfileService: TeacherProfileService,
    private val clock: Clock
): GetTeachersWithExpiredSubscriptionUseCase {

    override fun getAll(): List<UUID> {
        val now = clock.instant().toEpochMilli()
        val dayAgo = now - DateUtils.DAY_MILLIS
        return teacherProfileService.getTeachersWithExpiredSubscriptionBetween(dayAgo, now)
            .map { it.id }
    }
}
