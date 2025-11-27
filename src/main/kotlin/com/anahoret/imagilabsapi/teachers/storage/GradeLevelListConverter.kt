package com.anahoret.imagilabsapi.teachers.storage

import com.anahoret.imagilabsapi.teachers.domain.GradeLevel
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * JPA AttributeConverter for converting between List<GradeLevel> and comma-separated String.
 * This converter handles automatic conversion when persisting and retrieving GradeLevel lists
 * from the database.
 */
@Converter
class GradeLevelListConverter : AttributeConverter<List<GradeLevel>, String?> {

    override fun convertToDatabaseColumn(attribute: List<GradeLevel>?): String? {
        return attribute?.takeIf { it.isNotEmpty() }?.joinToString(",") { it.name }
    }

    override fun convertToEntityAttribute(dbData: String?): List<GradeLevel> {
        return dbData?.split(",")
            ?.mapNotNull {
                try {
                    GradeLevel.valueOf(it.trim())
                } catch (e: IllegalArgumentException) {
                    null
                }
            } ?: emptyList()
    }
}
