package com.anahoret.imagilabsapi.edlink.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/**
 * EdLink Subject model representing an academic subject
 * @see <a href="https://ed.link/docs/api/v2.0/models/subject">EdLink Subject Documentation</a>
 */
class Subject(
    @field:JsonProperty("id") val id: UUID,
    @field:JsonProperty("name") val name: String
)