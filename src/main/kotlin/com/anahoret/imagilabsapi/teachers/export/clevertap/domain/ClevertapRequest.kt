package com.anahoret.imagilabsapi.teachers.export.clevertap.domain

@Suppress("unused")
data class ClevertapRequest(
    val identity: String,
    val type: String,
    val evtName: String,
    val evtData: String? = null
)
