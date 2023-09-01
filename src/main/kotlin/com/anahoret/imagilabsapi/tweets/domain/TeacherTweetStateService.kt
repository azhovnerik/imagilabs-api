package com.anahoret.imagilabsapi.tweets.domain

import com.anahoret.imagilabsapi.tweets.storage.TeacherTweetStateEntity
import com.anahoret.imagilabsapi.tweets.storage.TeacherTweetStateRepository
import org.springframework.stereotype.Service
import java.util.*

interface TeacherTweetStateService {

    fun create(teacherId: UUID): TeacherTweetsState
    fun getByTeacherId(teacherId: UUID): TeacherTweetsState
    fun update(teacherId: UUID, isHidden: Boolean): TeacherTweetsState
}

@Service
class TeacherTweetStateServiceImpl(
    private val teacherTweetStateRepository: TeacherTweetStateRepository
): TeacherTweetStateService {

    override fun create(teacherId: UUID): TeacherTweetsState {
        return createTeacherTweetsState(teacherId)
    }

    override fun getByTeacherId(teacherId: UUID): TeacherTweetsState {
        return teacherTweetStateRepository.findByTeacherId(teacherId)
            ?.let(TeacherTweetsState::mapFromEntity)
            ?: createTeacherTweetsState(teacherId)
    }

    override fun update(teacherId: UUID, isHidden: Boolean): TeacherTweetsState {
        return teacherTweetStateRepository.findByTeacherId(teacherId)
            ?.let {
                it.isHidden = isHidden
                it.isSowedFeedbackDialog = true
                teacherTweetStateRepository.save(it)
            }
            ?.let(TeacherTweetsState::mapFromEntity)
            ?: createTeacherTweetsState(teacherId, isHidden)
    }

    private fun createTeacherTweetsState(teacherId: UUID, isHidden: Boolean = false): TeacherTweetsState {
        return teacherTweetStateRepository.save(TeacherTweetStateEntity(teacherId = teacherId, isHidden = isHidden))
            .let(TeacherTweetsState::mapFromEntity)
    }
}
