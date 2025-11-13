package com.anahoret.imagilabsapi.students.storage

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface StudentProfileEntityRepository : CrudRepository<StudentProfileEntity, UUID> {
    @Query(
        """
        SELECT sp FROM StudentProfileEntity sp
        JOIN StudentClassroomEntity sc ON sc.studentId = sp.id
        JOIN ClassroomEntity cr ON cr.id = sc.classroomId
        WHERE
            sp.username = :username AND
            sp.password = :password AND
            cr.accessCode = :classroomAccessCode            
        """
    )
    fun findByCredentials(username: String, password: String, classroomAccessCode: String): StudentProfileEntity?
    fun deleteByIdIn(studentIds: Collection<UUID>)

    @Query("SELECT COALESCE(s.aiChatOnboardingCompleted, FALSE) FROM StudentProfileEntity s WHERE s.id = :studentId")
    fun isAiChatOnboardingCompleted(studentId: UUID): Boolean
    fun findOneByEdLinkIntegrationIdAndEdLinkPersonId(
        edLinkIntegrationId: UUID,
        edLinkPersonId: UUID
    ): StudentProfileEntity?
}
