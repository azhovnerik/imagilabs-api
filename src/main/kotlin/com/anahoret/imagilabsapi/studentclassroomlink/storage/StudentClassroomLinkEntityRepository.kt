package com.anahoret.imagilabsapi.studentclassroomlink.storage;

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
}

interface ClassroomStudentCount {

    val classroomId: UUID
    val studentsCount: Long
}
