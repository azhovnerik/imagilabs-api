package com.anahoret.imagilabsapi.edlink.api.model

import com.fasterxml.jackson.annotation.JsonProperty

class CodeTokenExchangeRequest(
    @field:JsonProperty("code")
    val code: String,

    @field:JsonProperty("client_id")
    val clientId: String,

    @field:JsonProperty("client_secret")
    val clientSecret: String,

    @field:JsonProperty("redirect_uri")
    val redirectUri: String,

    @field:JsonProperty("grant_type")
    val grantType: String = "authorization_code"
)
