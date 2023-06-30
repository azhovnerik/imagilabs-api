package com.anahoret.imagilabsapi.teachers.export.clevertap.domain

@Suppress("unused")
class ClevertapResponse(
    val status: String,
    val processed: Int,
    val unprocessed: List<Unprocessed>
)

@Suppress("unused")
class Unprocessed(
    val status: String,
    val code: Int,
    val error: String,
    val record: Any
)
