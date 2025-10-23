package com.anahoret.imagilabsapi.edlink.domain

import java.util.*

class EdLinkOAuthCallbackRequest(val state: UUID, val code: String, val mobileAppClient: Boolean)
