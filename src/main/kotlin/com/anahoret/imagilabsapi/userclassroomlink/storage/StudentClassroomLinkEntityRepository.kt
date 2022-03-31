package com.anahoret.imagilabsapi.userclassroomlink.storage

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface StudentClassroomLinkEntityRepository : CrudRepository<StudentClassroomLinkEntity, UUID> {

    @Query(
        """
            SELECT 
                CAST (cs.classroom_id AS TEXT) AS classroomId,
                COUNT(cs.student_id) AS studentsCount                
            FROM classrooms_students cs
            WHERE cs.classroom_id IN :classroomIds
            GROUP BY cs.classroom_id
        """,
        nativeQuery = true
    )
    fun getStudentCounts(classroomIds: Iterable<UUID>): Iterable<ClassroomStudentCount>
    fun countByClassroomId(classroomId: UUID): Long
    fun findAllByClassroomId(classroomId: UUID): Iterable<StudentClassroomLinkEntity>
    fun existsByStudentIdAndClassroomId(studentId: UUID, classroomId: UUID): Boolean
    fun findAllByStudentId(studentId: UUID): Iterable<StudentClassroomLinkEntity>
    fun deleteAllByStudentId(studentId: UUID)
    fun deleteAllByStudentIdIn(studentIds: Collection<UUID>)

    @Query(
        """
        SELECT c.teacherId FROM StudentClassroomLinkEntity scl
        JOIN ClassroomEntity c ON c.id = scl.classroomId
        WHERE scl.studentId = :studentId
    """
    )
    fun findAllStudentClassroomsTeacherIds(studentId: UUID): Set<UUID>
}

interface ClassroomStudentCount {

    val classroomId: UUID
    val studentsCount: Long
}
