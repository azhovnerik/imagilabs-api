package com.anahoret.imagilabsapi.teachers.storage

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface TeacherProfileEntityRepository : JpaRepository<TeacherProfileEntity, UUID> {

    fun findByEmail(email: String): TeacherProfileEntity?
    fun existsByEmail(email: String): Boolean

    @Query(
        """
        SELECT t FROM TeacherProfileEntity t
        WHERE
            LOWER(CONCAT(t.firstName, ' ', t.lastName)) LIKE CONCAT('%', LOWER(:searchQuery), '%') OR
            t.email LIKE CONCAT('%', LOWER(:searchQuery), '%')
    """
    )
    fun findAll(searchQuery: String, sort: Sort): Iterable<TeacherProfileEntity>
    fun findAllByIdNotIn(exclude: List<UUID>, sort: Sort): List<TeacherProfileEntity>

    fun findAllBySubscriptionStartIsNotNullAndSubscriptionEndBetween(leftRange: Long, rightRange: Long): List<TeacherProfileEntity>

    @Query("SELECT COALESCE(t.aiChatOnboardingCompleted, FALSE) FROM TeacherProfileEntity t WHERE t.id = :teacherId")
    fun isAiChatOnboardingCompleted(teacherId: UUID): Boolean
}
