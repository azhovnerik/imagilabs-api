package com.anahoret.imagilabsapi.subscription.domain

import java.time.LocalDate

data class SetSubscriptionPeriodRequest(
    val startDate: LocalDate,
    val endDate: LocalDate
)
