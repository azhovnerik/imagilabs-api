package com.anahoret.imagilabsapi.students.storage

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface StudentProfileEntityRepository : CrudRepository<StudentProfileEntity, UUID> {

    @Query(
        """
        SELECT sp FROM StudentProfileEntity sp
        JOIN StudentClassroomLinkEntity scl ON scl.studentId = sp.id
        JOIN ClassroomEntity cr ON cr.id = scl.classroomId
        WHERE cr.id = :classroomId                     
        """
    )
    fun findAllByClassroom(classroomId: UUID): Iterable<StudentProfileEntity>

    @Query(
        """
        SELECT sp FROM StudentProfileEntity sp
        JOIN StudentClassroomLinkEntity scl ON scl.studentId = sp.id
        JOIN ClassroomEntity cr ON cr.id = scl.classroomId
        WHERE
            sp.username = :username AND
            sp.password = :password AND
            cr.accessCode = :classroomAccessCode            
        """
    )
    fun findByCredentials(username: String, password: String, classroomAccessCode: String): StudentProfileEntity?
    fun deleteByIdIn(studentIds: Collection<UUID>)
}
