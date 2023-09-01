package com.anahoret.imagilabsapi.tweets.domain

class ChangeTeacherTweetsStateRequest(
    val isHidden: Boolean
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChangeTeacherTweetsStateRequest

        return isHidden == other.isHidden
    }

    override fun hashCode(): Int {
        return isHidden.hashCode()
    }
}
