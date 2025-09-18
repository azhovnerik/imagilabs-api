package com.anahoret.imagilabsapi.lovable.storage

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface LovableClassroomEntityRepository : JpaRepository<LovableClassroomEntity, UUID> {
    fun findOneByClassroomId(classroomId: UUID): LovableClassroomEntity?
}
