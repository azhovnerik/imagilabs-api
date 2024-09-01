package com.anahoret.imagilabsapi.subscription.domain

interface TeacherSubscriptionData {
    val subscriptionStart: Long?
    val subscriptionEnd: Long?
    val subscriptionCanceled: Boolean

    fun hasProSubscription(now: Long): Boolean {
        return subscriptionStart != null && subscriptionEnd != null && now < subscriptionEnd!! && now > subscriptionStart!!
    }
}
