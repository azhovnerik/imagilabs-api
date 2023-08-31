package com.anahoret.imagilabsapi.tweets.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.*

@Entity
@Table(
    name = "teacher_tweets_state",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uc_teacher_tweets_state_teacher_id",
            columnNames = ["teacher_id"]
        )
    ]
)
class TeacherTweetStateEntity(

    @Column(name = "teacher_id", nullable = false)
    var teacherId: UUID,

    @Column(name = "is_hidden", nullable = false)
    var isHidden: Boolean = false,

    @Column(name = "is_showed_feedback_dialog", nullable = false)
    var isSowedFeedbackDialog: Boolean = false

): BaseEntity()
