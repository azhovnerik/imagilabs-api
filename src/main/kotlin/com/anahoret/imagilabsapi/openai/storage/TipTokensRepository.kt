package com.anahoret.imagilabsapi.openai.storage

import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface TipTokensRepository : CrudRepository<StudentProfileEntity, UUID> {

    @Query(
        """
            SELECT tipTokens
            FROM StudentProfileEntity
            WHERE id = :studentId
        """
    )
    fun getStudentTipToken(studentId: UUID): Int

    @Modifying
    @Query(
        """
        UPDATE StudentProfileEntity 
        SET tipTokens = :tipTokens
    """
    )
    fun updateTipTokens(tipTokens: Int)

    @Query(
        """
        SELECT (tipTokens > 0) FROM StudentProfileEntity WHERE id = :studentId
    """
    )
    fun hasTipTokens(studentId: UUID): Boolean
}
