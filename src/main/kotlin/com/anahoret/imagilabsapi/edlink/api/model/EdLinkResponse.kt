package com.anahoret.imagilabsapi.edlink.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class EdLinkResponseSingle<T>(
    @field:JsonProperty($$"$data") val data: T,
    @field:JsonProperty($$"$request") val request: UUID
)

class EdLinkResponseList<T>(
    @field:JsonProperty($$"$data") val data: List<T>,
    @field:JsonProperty($$"$request") val request: UUID
)
