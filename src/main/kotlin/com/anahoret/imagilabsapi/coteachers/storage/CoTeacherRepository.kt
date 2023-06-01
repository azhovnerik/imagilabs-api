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
    @Query("""
        SELECT ct.id AS id, ct.classroomId AS classroomId, 
               ct.teacherId AS teacherId, ct.teacherEmail AS teacherEmail,
               tp.firstName AS firstName, tp.lastName AS lastName
        FROM CoTeacherEntity ct
        LEFT JOIN TeacherProfileEntity tp ON ct.teacherId = tp.id
        WHERE ct.classroomId = :classroomId
        GROUP BY ct.id, tp.firstName, tp.lastName
    """)
    fun findAllByClassroomId(classroomId: UUID): List<CoTeacherData>
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

interface CoTeacherData {
    val id: UUID
    val classroomId: UUID
    val teacherEmail: String
    val teacherId: UUID?
    val firstName: String?
    val lastName: String?
}
