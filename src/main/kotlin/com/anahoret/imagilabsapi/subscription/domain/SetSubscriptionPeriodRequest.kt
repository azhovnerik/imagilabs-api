package com.anahoret.imagilabsapi.subscription.domain

data class SetSubscriptionPeriodRequest(
    val startDate: Long,
    val endDate: Long
)
