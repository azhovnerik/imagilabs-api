package com.anahoret.imagilabsapi.openai.storage

import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface TipTokensRepository : CrudRepository<StudentProfileEntity, UUID> {

    @Modifying
    @Query(
        """
        UPDATE StudentProfileEntity 
        SET tipTokens = :tipTokens
    """
    )
    fun updateTipTokens(tipTokens: Int)
    @Query("""
        SELECT (tipTokens > 0) FROM StudentProfileEntity WHERE id = :studentId
    """)
    fun hasTipTokens(studentId: UUID): Boolean
}
