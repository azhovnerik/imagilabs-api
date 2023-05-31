package com.anahoret.imagilabsapi.coteachers.storage

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface CoTeacherRepository : JpaRepository<CoTeacherEntity, UUID> {

    @Query(
        """
        SELECT classroomId FROM CoTeacherEntity
        WHERE teacherId = :teacherId
    """
    )
    fun getClassroomIdsByTeacherId(teacherId: UUID): List<UUID>
    fun findAllByClassroomId(classroomId: UUID): List<CoTeacherEntity>
    fun existsByClassroomIdAndTeacherId(classroomId: UUID, teacherId: UUID): Boolean
    fun findByClassroomIdAndTeacherId(classroomId: UUID, teacherId: UUID): CoTeacherEntity?
    fun countAllByClassroomId(classroomId: UUID): Long

    @Query(
        """
        SELECT
        CAST(ct.classroom_id AS TEXT) AS classroomId,
        COUNT(ct.id) as coTeacherCount
        FROM co_teachers ct
        WHERE ct.classroom_id IN :classroomIds
        GROUP BY ct.classroom_id
    """,
        nativeQuery = true
    )
    fun getCoTeacherCountsByClassrooms(classroomIds: Iterable<UUID>): Iterable<ClassroomCoTeacherCount>
}

interface ClassroomCoTeacherCount {
    val classroomId: UUID
    val coTeacherCount: Long
}
