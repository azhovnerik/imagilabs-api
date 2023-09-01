package com.anahoret.imagilabsapi.tweets.storage

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "teacher_tweets_state")
class TeacherTweetStateEntity(

    @Id
    @Column(name = "teacher_id", nullable = false)
    var teacherId: UUID,

    @Column(name = "is_hidden", nullable = false)
    var isHidden: Boolean = false,

    @Column(name = "is_showed_feedback_dialog", nullable = false)
    var isSowedFeedbackDialog: Boolean = false

)
