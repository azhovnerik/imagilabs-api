package com.anahoret.imagilabsapi.teachingmaterials.domain

import java.util.*

class BundleLesson(
    val id: UUID,
    val bundleId: UUID,
    val index: Int,
    val name: String,
    val worksheetUri: String,
    val locked: Boolean
)
