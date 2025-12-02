package com.anahoret.imagilabsapi.edlink.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/**
 * EdLink Enrollment model representing a person's enrollment in a class
 * @see <a href="https://ed.link/docs/api/v2.0/models/enrollment">EdLink Enrollment Documentation</a>
 */
class Enrollment(
    @field:JsonProperty("id") val id: UUID,
    @field:JsonProperty("person_id") val personId: UUID,
    @field:JsonProperty("class_id") val classId: UUID,
    @field:JsonProperty("role") val role: String,
    @field:JsonProperty("state") val state: String
)