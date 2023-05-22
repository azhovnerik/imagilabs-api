package com.anahoret.imagilabsapi.subscription.domain

class TeacherSubscription(
    val start: Long?,
    val end: Long?,
    val plan: TeacherSubscriptionPlan,
    val canceled: Boolean
)

enum class TeacherSubscriptionPlan {
    STANDARD, PRO
}
