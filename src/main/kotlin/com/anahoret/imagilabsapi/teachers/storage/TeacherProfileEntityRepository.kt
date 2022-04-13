package com.anahoret.imagilabsapi.teachers.storage

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface TeacherProfileEntityRepository : PagingAndSortingRepository<TeacherProfileEntity, UUID> {

    fun findByEmail(email: String): TeacherProfileEntity?
    fun existsByEmail(email: String): Boolean

    @Query(
        """
        SELECT t FROM TeacherProfileEntity t
        WHERE LOWER(CONCAT(t.firstName, ' ', t.lastName)) LIKE CONCAT('%', LOWER(:searchQuery), '%')
    """
    )
    fun findAll(searchQuery: String, sort: Sort): Iterable<TeacherProfileEntity>
}
