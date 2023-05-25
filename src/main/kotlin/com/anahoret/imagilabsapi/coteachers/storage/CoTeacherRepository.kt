package com.anahoret.imagilabsapi.coteachers.storage

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface CoTeacherRepository: JpaRepository<CoTeacherEntity, UUID> {

    @Query("""
        SELECT classroomId FROM CoTeacherEntity
        WHERE teacherId = :teacherId
    """)
    fun getClassroomIdsByTeacherId(teacherId: UUID): List<UUID>
    fun findAllByClassroomId(classroomId: UUID): List<CoTeacherEntity>
    fun existsByClassroomIdAndTeacherId(classroomId: UUID, teacherId: UUID): Boolean
}
