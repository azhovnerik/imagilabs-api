package com.anahoret.imagilabsapi.edlink.domain

import com.anahoret.imagilabsapi.teachers.domain.GradeLevel
import com.anahoret.imagilabsapi.teachers.domain.SchoolRole
import com.anahoret.imagilabsapi.teachers.storage.GradeLevelListConverter
import com.anahoret.imagilabsapi.teachers.storage.SchoolRoleListConverter
import org.springframework.stereotype.Component

/**
 * Helper component to map EdLink API strings to our domain enums
 * Uses existing JPA converters for consistency
 */
@Component
class EdLinkEnumMapper {

    private val gradeLevelConverter = GradeLevelListConverter()
    private val schoolRoleConverter = SchoolRoleListConverter()

    /**
     * Maps EdLink grade level strings to List<GradeLevel>
     * @param gradeLevels list of grade level strings from EdLink API (e.g., ["first_grade", "second_grade"])
     */
    fun mapGradeLevels(gradeLevels: List<String>): List<GradeLevel> {
        if (gradeLevels.isEmpty()) return emptyList()
        val commaSeparated = gradeLevels.joinToString(",") { it.uppercase() }
        return gradeLevelConverter.convertToEntityAttribute(commaSeparated)
    }

    /**
     * Maps EdLink role strings to List<SchoolRole>
     * @param roles list of role strings from EdLink API (e.g., ["teacher", "administrator"])
     */
    fun mapSchoolRoles(roles: List<String>): List<SchoolRole> {
        if (roles.isEmpty()) return emptyList()
        // Convert to uppercase to match enum names (e.g., "teacher" -> "TEACHER")
        val commaSeparated = roles.joinToString(",") { it.uppercase() }
        return schoolRoleConverter.convertToEntityAttribute(commaSeparated)
    }

}
