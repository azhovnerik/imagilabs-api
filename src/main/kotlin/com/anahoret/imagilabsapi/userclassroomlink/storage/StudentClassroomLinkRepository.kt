package com.anahoret.imagilabsapi.userclassroomlink.storage

import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface StudentClassroomLinkRepository : CrudRepository<StudentProfileEntity, UUID> {

    @Query(
        """
            SELECT
                CAST (sp.classroom_id AS TEXT) AS classroomid,
                COUNT(sp.id) AS studentscount
            FROM student_profiles sp
            WHERE sp.classroom_id IN :classroomIds
            GROUP BY sp.classroom_id
        """,
        nativeQuery = true
    )
    fun getStudentCounts(classroomIds: Iterable<UUID>): Iterable<ClassroomStudentCount>

}

interface ClassroomStudentCount {

    val classroomId: UUID
    val studentsCount: Long
}
