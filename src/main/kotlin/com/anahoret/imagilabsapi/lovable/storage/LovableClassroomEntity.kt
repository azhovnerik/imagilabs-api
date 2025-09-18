package com.anahoret.imagilabsapi.lovable.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "lovable_classrooms")
class LovableClassroomEntity(
    @Column(name = "classroom_id")
    var classroomId: UUID,

    @Column(name = "lovable_integration_enabled")
    var lovableIntegrationEnabled: Boolean = true,

    @Column(name = "lovable_integration_paused")
    var lovableIntegrationPaused: Boolean = false
) : BaseEntity()
