package com.anahoret.imagilabsapi.edlink.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class School(
    @field:JsonProperty("id") val id: UUID,
    @field:JsonProperty("name") val name: String
)
