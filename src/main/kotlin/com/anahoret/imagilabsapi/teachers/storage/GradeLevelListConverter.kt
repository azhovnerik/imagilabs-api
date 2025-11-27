package com.anahoret.imagilabsapi.teachers.storage

import com.anahoret.imagilabsapi.common.storage.EnumListConverter
import com.anahoret.imagilabsapi.teachers.domain.GradeLevel
import jakarta.persistence.Converter

/**
 * JPA AttributeConverter for converting between List<GradeLevel> and comma-separated String.
 * This converter handles automatic conversion when persisting and retrieving GradeLevel lists
 * from the database.
 */
@Converter
class GradeLevelListConverter : EnumListConverter<GradeLevel>(GradeLevel::class.java)
