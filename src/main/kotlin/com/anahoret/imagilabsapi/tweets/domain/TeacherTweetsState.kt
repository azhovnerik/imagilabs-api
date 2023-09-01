package com.anahoret.imagilabsapi.tweets.domain

import com.anahoret.imagilabsapi.tweets.storage.TeacherTweetStateEntity
import java.util.*

@Suppress("unused")
class TeacherTweetsState(
    val teacherId: UUID,
    val isHidden: Boolean,
    val isSowedFeedbackDialog: Boolean
) {

    companion object {

        fun mapFromEntity(entity: TeacherTweetStateEntity): TeacherTweetsState {
            return with(entity) { TeacherTweetsState(teacherId, isHidden, isSowedFeedbackDialog) }
        }
    }
}
