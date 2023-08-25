package com.anahoret.imagilabsapi.teacherchecklist.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "teacher_checklist_steps")
class TeacherCheckListStepEntity(

    @Column(name = "teacher_id", nullable = false)
    var teacherId: UUID,

    @Column(name = "step", nullable = false)
    @Enumerated(value = EnumType.STRING)
    var step: TeacherCheckListStep,

    @Column(name = "completed", nullable = false)
    var completed: Boolean = false

) : BaseEntity()
