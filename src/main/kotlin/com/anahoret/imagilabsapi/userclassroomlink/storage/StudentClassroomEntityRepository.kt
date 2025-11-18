package com.anahoret.imagilabsapi.userclassroomlink.storage

import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface StudentClassroomEntityRepository : CrudRepository<StudentClassroomEntity, StudentClassroomId> {
    fun findAllByStudentId(studentId: UUID): Iterable<StudentClassroomEntity>
    fun deleteByStudentIdAndClassroomId(studentId: UUID, classroomId: UUID)
    fun existsByStudentIdAndClassroomId(studentId: UUID, classroomId: UUID): Boolean
    fun findAllByClassroomId(classroomId: UUID): Iterable<StudentClassroomEntity>
    fun countByClassroomId(classroomId: UUID): Long

    @Query(
        """
            SELECT
                sce.classroomId AS classroomid,
                COUNT (DISTINCT sce.studentId) AS studentscount
            FROM StudentClassroomEntity sce
            WHERE sce.classroomId IN :classroomIds
            GROUP BY sce.classroomId
        """
    )
    fun getStudentCounts(classroomIds: Iterable<UUID>): Iterable<ClassroomStudentCount>

    @Query(
        """SELECT DISTINCT sp FROM StudentProfileEntity sp
                JOIN StudentClassroomEntity sc ON sc.studentId = sp.id
                WHERE sc.classroomId = :classroomId"""
    )
    fun findAllByClassroomId(classroomId: UUID, sort: Sort): Iterable<StudentProfileEntity>

    @Query(
        """
        SELECT DISTINCT sp FROM StudentProfileEntity sp
        JOIN StudentClassroomEntity sc ON sc.studentId = sp.id
        WHERE sc.classroomId = :classroomId AND LOWER(sp.name) LIKE CONCAT('%', LOWER(:searchQuery) , '%') 
    """
    )
    fun findAllByClassroomId(classroomId: UUID, searchQuery: String, sort: Sort): Iterable<StudentProfileEntity>

    @Query("SELECT sc.classroomId FROM StudentClassroomEntity sc WHERE sc.studentId = :studentId")
    fun findAllClassroomIdsByStudentId(studentId: UUID): List<UUID>
}

interface ClassroomStudentCount {

    val classroomId: UUID
    val studentsCount: Long
}
