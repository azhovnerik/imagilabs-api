package com.anahoret.imagilabsapi.teachers.storage

import com.anahoret.imagilabsapi.common.storage.EnumListConverter
import com.anahoret.imagilabsapi.teachers.domain.SchoolRole
import jakarta.persistence.Converter

/**
 * JPA AttributeConverter for converting between List<SchoolRole> and comma-separated String.
 * This converter handles automatic conversion when persisting and retrieving SchoolRole lists
 * from the database.
 */
@Converter
class SchoolRoleListConverter : EnumListConverter<SchoolRole>(SchoolRole::class.java)