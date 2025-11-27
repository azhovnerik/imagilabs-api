package com.anahoret.imagilabsapi.teachers.storage

import com.anahoret.imagilabsapi.teachers.domain.SchoolRole
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * JPA AttributeConverter for converting between List<SchoolRole> and comma-separated String.
 * This converter handles automatic conversion when persisting and retrieving SchoolRole lists
 * from the database.
 */
@Converter
class SchoolRoleListConverter : AttributeConverter<List<SchoolRole>, String?> {

    override fun convertToDatabaseColumn(attribute: List<SchoolRole>?): String? {
        return attribute?.takeIf { it.isNotEmpty() }?.joinToString(",") { it.name }
    }

    override fun convertToEntityAttribute(dbData: String?): List<SchoolRole> {
        return dbData?.split(",")
            ?.mapNotNull {
                try {
                    SchoolRole.valueOf(it.trim())
                } catch (e: IllegalArgumentException) {
                    null
                }
            } ?: emptyList()
    }
}