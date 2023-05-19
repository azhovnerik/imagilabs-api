package com.anahoret.imagilabsapi.subscription.domain

class TeacherSubscription(
    val start: Long?,
    val end: Long?,
    val plan: TeacherSubscriptionPlan
)

enum class TeacherSubscriptionPlan {
    STANDARD, PRO
}
