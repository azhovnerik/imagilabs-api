package com.anahoret.imagilabsapi.edlink.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/**
 * Extended person data from EdLink Graph API v2/graph/people/:person_id
 * Contains additional fields not available in v2/my/profile
 */
class PersonDetails(
    @field:JsonProperty("id") val id: UUID,
    @field:JsonProperty("state") val state: String?,
    @field:JsonProperty("district_id") val districtId: UUID?
)
