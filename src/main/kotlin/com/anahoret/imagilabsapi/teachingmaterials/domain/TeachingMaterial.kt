package com.anahoret.imagilabsapi.teachingmaterials.domain

import com.anahoret.imagilabsapi.teachingmaterials.storage.TeachingMaterialEntity
import java.util.*

class TeachingMaterial(
    val id: UUID,
    val index: Int,
    val name: String,
    val path: String,
    val isExternalLink: Boolean
) {

    companion object {

        fun fromEntity(teachingMaterialEntity: TeachingMaterialEntity): TeachingMaterial {
            return with(teachingMaterialEntity) {
                TeachingMaterial(id!!, index, name, path, isExternalLink)
            }
        }
    }
}
