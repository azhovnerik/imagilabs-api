package com.anahoret.imagilabsapi.common.storage

import jakarta.persistence.AttributeConverter

/**
 * Base converter for converting between List<Enum> and comma-separated String.
 * Specific enum converters should extend this class and provide the enum class type.
 *
 * This converter handles automatic conversion when persisting and retrieving enum lists
 * from the database.
 *
 * @param E the enum type
 * @property enumClass the Class object of the enum type
 */
abstract class EnumListConverter<E : Enum<E>>(
    private val enumClass: Class<E>
) : AttributeConverter<List<E>, String?> {

    override fun convertToDatabaseColumn(attribute: List<E>?): String? {
        return attribute?.takeIf { it.isNotEmpty() }?.joinToString(",") { it.name }
    }

    override fun convertToEntityAttribute(dbData: String?): List<E> {
        return dbData?.split(",")
            ?.mapNotNull {
                try {
                    java.lang.Enum.valueOf(enumClass, it.trim())
                } catch (e: IllegalArgumentException) {
                    null
                }
            } ?: emptyList()
    }
}
