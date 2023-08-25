package com.anahoret.imagilabsapi.teachers.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface TeacherStatisticRepository: JpaRepository<BaseEntity, UUID> {

    @Query("""
        SELECT active_classrooms AS activeClassrooms, 
               student_accounts  AS studentAccounts, 
               shared_projects   AS studentSharedProjects, 
               draft_projects    AS studentDraftProjects
        FROM teacher_statistic_view
        WHERE id = :teacherId
    """, nativeQuery = true)
    fun getTeacherStatistic(teacherId: UUID): TeacherStatistic?
}

interface TeacherStatistic {
    val activeClassrooms: Long
    val studentAccounts: Long
    val studentSharedProjects: Long
    val studentDraftProjects: Long
}
