package com.anahoret.imagilabsapi.subscription.domain

class TeacherSubscription(
    val start: Long?,
    val end: Long?,
    val plan: TeacherSubscriptionPlan,
    val canceled: Boolean
) : TeacherSubscriptionData {
    override val subscriptionStart = start
    override val subscriptionEnd = end
    override val subscriptionCanceled = canceled
}

enum class TeacherSubscriptionPlan {
    STANDARD, PRO
}
