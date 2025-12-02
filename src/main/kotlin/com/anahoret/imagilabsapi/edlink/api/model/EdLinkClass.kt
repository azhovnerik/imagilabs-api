package com.anahoret.imagilabsapi.edlink.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class EdLinkClass(
    @field:JsonProperty("id") val id: UUID,
    @field:JsonProperty("school_id") val schoolId: UUID,
    @field:JsonProperty("name") val name: String,
    @field:JsonProperty("subject_id") val subjectId: UUID?
)
