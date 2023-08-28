package com.anahoret.imagilabsapi.teachingmaterials.storage

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface TeacherBundleRepository: JpaRepository<TeacherBundleEntity, UUID> {

    fun deleteAllByTeacherId(teacherId: UUID)
    fun findByTeacherIdAndBundleId(teacherId: UUID, bundleId: UUID): TeacherBundleEntity?
    fun existsByTeacherIdAndBundleId(teacherId: UUID, bundleId: UUID): Boolean
}
