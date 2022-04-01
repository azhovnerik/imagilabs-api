package com.anahoret.imagilabsapi.students.storage

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface StudentProfileEntityRepository : CrudRepository<StudentProfileEntity, UUID> {

    fun findAllByClassroomId(classroomId: UUID): Iterable<StudentProfileEntity>

    @Query(
        """
        SELECT sp FROM StudentProfileEntity sp
        JOIN ClassroomEntity cr ON cr.id = sp.classroomId
        WHERE
            sp.username = :username AND
            sp.password = :password AND
            cr.accessCode = :classroomAccessCode            
        """
    )
    fun findByCredentials(username: String, password: String, classroomAccessCode: String): StudentProfileEntity?
    fun deleteByIdIn(studentIds: Collection<UUID>)
    fun countByClassroomId(classroomId: UUID): Long
    fun existsByIdAndClassroomId(studentId: UUID, classroomId: UUID): Boolean
}
