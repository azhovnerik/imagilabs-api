package com.anahoret.imagilabsapi.tweets.storage

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface TeacherTweetStateRepository: JpaRepository<TeacherTweetStateEntity, UUID> {

    fun findByTeacherId(teacherId: UUID): TeacherTweetStateEntity?
}
