package com.anahoret.imagilabsapi.edlink.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class Integration(
    @field:JsonProperty("id") val id: UUID,
    @field:JsonProperty("access_token") val accessToken: String
)

class MyIntegration(
    @field:JsonProperty("id") val id: UUID
)
