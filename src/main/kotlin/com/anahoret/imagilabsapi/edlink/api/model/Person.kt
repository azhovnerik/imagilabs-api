package com.anahoret.imagilabsapi.edlink.api.model

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.UnsupportedUserTypeError
import com.anahoret.imagilabsapi.users.UserType
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class Person(
    @field:JsonProperty("id") val id: UUID,
    @field:JsonProperty("email") val email: String,
    @field:JsonProperty("first_name") val firstName: String,
    @field:JsonProperty("last_name") val lastName: String,
    @field:JsonProperty("display_name") val displayName: String,
    @field:JsonProperty("roles") val roles: List<String>,
    @field:JsonProperty("address") val address: Address,
    @field:JsonProperty("state") val state: String,
    @field:JsonProperty("grade_levels") val grades: List<String>,
    @field:JsonProperty("subjects") val subjects: String,
    @field:JsonProperty("schools") val schools: List<UUID>,
) {
    fun getUserType(): Either<UnsupportedUserTypeError, UserType> {
        return when {
            roles.contains("student") -> UserType.STUDENT.right()
            roles.contains("teacher") -> UserType.TEACHER.right()
            else -> UnsupportedUserTypeError.left()
        }
    }
}

class Address(
    @field:JsonProperty("country") val country: String?
)
