package com.anahoret.imagilabsapi.subscription.domain

interface TeacherSubscriptionData {
    val subscriptionStart: Long?
    val subscriptionEnd: Long?
    val subscriptionCanceled: Boolean
}
