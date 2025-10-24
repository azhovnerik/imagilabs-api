package com.anahoret.imagilabsapi.edlink.api.model

import com.fasterxml.jackson.annotation.JsonProperty

class CodeTokenExchangeResponse(
    @field:JsonProperty("access_token")
    val accessToken: String,

    @field:JsonProperty("refresh_token")
    val refreshToken: String,

    @field:JsonProperty("token_type")
    val tokenType: String,

    @field:JsonProperty("expires_in")
    val expiresIn: Int
)
