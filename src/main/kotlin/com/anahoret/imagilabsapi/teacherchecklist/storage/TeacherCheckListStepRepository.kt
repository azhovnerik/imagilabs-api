package com.anahoret.imagilabsapi.teacherchecklist.storage

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface TeacherCheckListStepRepository: JpaRepository<TeacherCheckListStepEntity, UUID> {
    fun findAllByTeacherId(teacherId: UUID): List<TeacherCheckListStepEntity>
    fun findByTeacherIdAndStep(teacherId: UUID, step: TeacherCheckListStep): TeacherCheckListStepEntity
    fun findAllByTeacherIdAndCompletedIsFalse(teacherId: UUID): List<TeacherCheckListStepEntity>
}
